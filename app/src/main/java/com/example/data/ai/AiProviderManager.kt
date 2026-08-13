package com.example.data.ai

import com.example.data.preferences.AiSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiResponse(
    val text: String,
    val providerUsed: String, // "OpenRouter" or "Gemini"
    val modelUsed: String,
    val action: StructuredAiAction? = null,
    val isFailover: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

class AiProviderManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun processChatRequest(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>>, // Pair(sender, message)
        settings: AiSettings
    ): AiResponse = withContext(Dispatchers.IO) {
        if (!settings.aiEnabled) {
            return@withContext AiResponse(
                text = "AI Assistant is currently disabled in Settings.",
                providerUsed = "System",
                modelUsed = "None",
                isError = true,
                errorMessage = "AI Disabled"
            )
        }

        val isPrimaryOpenRouter = settings.primaryProvider.equals("OpenRouter", ignoreCase = true)

        if (isPrimaryOpenRouter) {
            // Try OpenRouter first
            val primaryResult = callOpenRouter(userPrompt, conversationHistory, settings)
            if (!primaryResult.isError) {
                return@withContext primaryResult
            }

            // Primary failed -> check Auto Failover
            if (settings.autoFailover) {
                val fallbackResult = callGemini(userPrompt, conversationHistory, settings, isFailover = true)
                if (!fallbackResult.isError) {
                    return@withContext fallbackResult
                }
                return@withContext AiResponse(
                    text = "Both AI Providers (OpenRouter & Gemini) failed.\nPrimary Error: ${primaryResult.errorMessage}\nFallback Error: ${fallbackResult.errorMessage}",
                    providerUsed = "Error",
                    modelUsed = "None",
                    isError = true,
                    errorMessage = "Dual Provider Failure"
                )
            } else {
                return@withContext primaryResult
            }
        } else {
            // Try Gemini first
            val primaryResult = callGemini(userPrompt, conversationHistory, settings)
            if (!primaryResult.isError) {
                return@withContext primaryResult
            }

            // Primary failed -> check Auto Failover
            if (settings.autoFailover) {
                val fallbackResult = callOpenRouter(userPrompt, conversationHistory, settings, isFailover = true)
                if (!fallbackResult.isError) {
                    return@withContext fallbackResult
                }
                return@withContext AiResponse(
                    text = "Both AI Providers (Gemini & OpenRouter) failed.\nPrimary Error: ${primaryResult.errorMessage}\nFallback Error: ${fallbackResult.errorMessage}",
                    providerUsed = "Error",
                    modelUsed = "None",
                    isError = true,
                    errorMessage = "Dual Provider Failure"
                )
            } else {
                return@withContext primaryResult
            }
        }
    }

    private fun callOpenRouter(
        userPrompt: String,
        history: List<Pair<String, String>>,
        settings: AiSettings,
        isFailover: Boolean = false
    ): AiResponse {
        val apiKey = settings.openRouterApiKey.trim()
        if (apiKey.isEmpty()) {
            return AiResponse(
                text = "OpenRouter API Key is missing. Please configure it in AI Settings.",
                providerUsed = "OpenRouter",
                modelUsed = settings.openRouterModel,
                isError = true,
                errorMessage = "Missing OpenRouter API Key"
            )
        }

        try {
            val messagesArray = JSONArray()

            // System prompt
            val systemObj = JSONObject()
            systemObj.put("role", "system")
            systemObj.put("content", settings.systemPrompt)
            messagesArray.put(systemObj)

            // Conversation history
            for (item in history.takeLast(10)) {
                val msgObj = JSONObject()
                msgObj.put("role", if (item.first == "USER") "user" else "assistant")
                msgObj.put("content", item.second)
                messagesArray.put(msgObj)
            }

            // Current user prompt
            val currentUserObj = JSONObject()
            currentUserObj.put("role", "user")
            currentUserObj.put("content", userPrompt)
            messagesArray.put(currentUserObj)

            val requestJson = JSONObject()
            requestJson.put("model", settings.openRouterModel)
            requestJson.put("messages", messagesArray)
            requestJson.put("temperature", settings.temperature)
            requestJson.put("max_tokens", settings.maxTokens)

            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://hisab.site.je")
                .addHeader("X-Title", "Hisab Tracker AI")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorDetail = extractErrorDetail(bodyString, response.code)
                    return AiResponse(
                        text = "OpenRouter Request Failed (HTTP ${response.code}): $errorDetail",
                        providerUsed = "OpenRouter",
                        modelUsed = settings.openRouterModel,
                        isError = true,
                        errorMessage = "HTTP ${response.code} - $errorDetail"
                    )
                }

                val responseJson = JSONObject(bodyString)
                val choices = responseJson.getJSONArray("choices")
                if (choices.length() == 0) {
                    return AiResponse(
                        text = "OpenRouter returned empty choices.",
                        providerUsed = "OpenRouter",
                        modelUsed = settings.openRouterModel,
                        isError = true,
                        errorMessage = "Empty Response"
                    )
                }

                val rawText = choices.getJSONObject(0).getJSONObject("message").getString("content")
                val action = AiActionParser.parseAction(rawText)

                return AiResponse(
                    text = rawText,
                    providerUsed = if (isFailover) "OpenRouter (Failover)" else "OpenRouter",
                    modelUsed = settings.openRouterModel,
                    action = action,
                    isFailover = isFailover,
                    isError = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return AiResponse(
                text = "OpenRouter Connection Error: ${e.message}",
                providerUsed = "OpenRouter",
                modelUsed = settings.openRouterModel,
                isError = true,
                errorMessage = e.message ?: "Unknown Exception"
            )
        }
    }

    private fun callGemini(
        userPrompt: String,
        history: List<Pair<String, String>>,
        settings: AiSettings,
        isFailover: Boolean = false
    ): AiResponse {
        val apiKey = settings.geminiApiKey.trim()
        if (apiKey.isEmpty()) {
            return AiResponse(
                text = "Google Gemini API Key is missing. Please configure it in AI Settings.",
                providerUsed = "Gemini",
                modelUsed = settings.geminiModel,
                isError = true,
                errorMessage = "Missing Gemini API Key"
            )
        }

        try {
            val contentsArray = JSONArray()

            // System instruction inside Gemini API payload
            val systemInstructionObj = JSONObject()
            val systemParts = JSONArray()
            val systemPart = JSONObject()
            systemPart.put("text", settings.systemPrompt)
            systemParts.put(systemPart)
            systemInstructionObj.put("parts", systemParts)

            // Conversation history
            for (item in history.takeLast(10)) {
                val contentObj = JSONObject()
                contentObj.put("role", if (item.first == "USER") "user" else "model")
                val partsArr = JSONArray()
                val p = JSONObject()
                p.put("text", item.second)
                partsArr.put(p)
                contentObj.put("parts", partsArr)
                contentsArray.put(contentObj)
            }

            // Current prompt
            val currentContentObj = JSONObject()
            currentContentObj.put("role", "user")
            val currentPartsArr = JSONArray()
            val cp = JSONObject()
            cp.put("text", userPrompt)
            currentPartsArr.put(cp)
            currentContentObj.put("parts", currentPartsArr)
            contentsArray.put(currentContentObj)

            val generationConfig = JSONObject()
            generationConfig.put("temperature", settings.temperature)
            generationConfig.put("maxOutputTokens", settings.maxTokens)

            val requestJson = JSONObject()
            requestJson.put("contents", contentsArray)
            requestJson.put("systemInstruction", systemInstructionObj)
            requestJson.put("generationConfig", generationConfig)

            val modelName = if (settings.geminiModel.isBlank()) "gemini-3.5-flash" else settings.geminiModel
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorDetail = extractErrorDetail(bodyString, response.code)
                    return AiResponse(
                        text = "Gemini Request Failed (HTTP ${response.code}): $errorDetail",
                        providerUsed = "Gemini",
                        modelUsed = modelName,
                        isError = true,
                        errorMessage = "HTTP ${response.code} - $errorDetail"
                    )
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return AiResponse(
                        text = "Gemini returned empty candidates response.",
                        providerUsed = "Gemini",
                        modelUsed = modelName,
                        isError = true,
                        errorMessage = "Empty Candidates"
                    )
                }

                val firstCandidate = candidates.getJSONObject(0)
                val parts = firstCandidate.getJSONObject("content").getJSONArray("parts")
                val rawText = parts.getJSONObject(0).getString("text")

                val action = AiActionParser.parseAction(rawText)

                return AiResponse(
                    text = rawText,
                    providerUsed = if (isFailover) "Gemini (Failover)" else "Gemini",
                    modelUsed = modelName,
                    action = action,
                    isFailover = isFailover,
                    isError = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return AiResponse(
                text = "Gemini Connection Error: ${e.message}",
                providerUsed = "Gemini",
                modelUsed = settings.geminiModel,
                isError = true,
                errorMessage = e.message ?: "Unknown Exception"
            )
        }
    }

    private fun extractErrorDetail(bodyString: String, code: Int): String {
        return try {
            val json = JSONObject(bodyString)
            if (json.has("error")) {
                val errObj = json.get("error")
                if (errObj is JSONObject) {
                    errObj.optString("message", bodyString)
                } else {
                    errObj.toString()
                }
            } else {
                bodyString.take(200)
            }
        } catch (e: Exception) {
            "HTTP Code $code"
        }
    }
}
