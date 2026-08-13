package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ai_settings_prefs")

data class AiSettings(
    val openRouterApiKey: String,
    val geminiApiKey: String,
    val primaryProvider: String, // "OpenRouter" or "Gemini"
    val fallbackProvider: String, // "Gemini" or "OpenRouter"
    val openRouterModel: String,
    val geminiModel: String,
    val temperature: Float,
    val maxTokens: Int,
    val autoFailover: Boolean,
    val aiEnabled: Boolean,
    val systemPrompt: String
)

class AiSettingsManager(private val context: Context) {

    companion object {
        val KEY_OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val KEY_GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val KEY_PRIMARY_PROVIDER = stringPreferencesKey("primary_provider")
        val KEY_FALLBACK_PROVIDER = stringPreferencesKey("fallback_provider")
        val KEY_OPENROUTER_MODEL = stringPreferencesKey("openrouter_model")
        val KEY_GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val KEY_TEMPERATURE = floatPreferencesKey("temperature")
        val KEY_MAX_TOKENS = intPreferencesKey("max_tokens")
        val KEY_AUTO_FAILOVER = booleanPreferencesKey("auto_failover")
        val KEY_AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val KEY_SYSTEM_PROMPT = stringPreferencesKey("system_prompt")

        const val DEFAULT_SYSTEM_PROMPT = """You are Hisab Tracker AI, an intelligent personal financial assistant.
Your job is to understand user requests in Bangla or English and execute structured financial actions on their Hisab Tracker database.
When a user asks to add, edit, delete, search, or set budgets/categories, you MUST respond in markdown with helpful financial advice AND include a JSON action block at the VERY END of your response inside triple backticks with tag `json`.

JSON Schema format:
```json
{
  "action": "CREATE_EXPENSE" | "CREATE_INCOME" | "UPDATE_EXPENSE" | "UPDATE_INCOME" | "DELETE_EXPENSE" | "DELETE_INCOME" | "SEARCH_TRANSACTIONS" | "CREATE_CATEGORY" | "CREATE_BUDGET" | "GENERATE_REPORT" | "ANALYZE_FINANCES",
  "amount": number (optional),
  "category": string (optional),
  "description": string (optional),
  "date": string "YYYY-MM-DD" (optional),
  "id": number (optional, for update/delete),
  "query": string (optional, for search),
  "type": string "INCOME" or "EXPENSE" (optional)
}
```

Rules:
1. Always analyze income, expenses, and savings accurately.
2. Supports Bangla natural language (e.g. "আজ ৫০০ টাকা খাবারের খরচ যোগ করো" -> CREATE_EXPENSE amount 500, category "Food & Dining") and English.
3. For DELETION requests, specify action "DELETE_EXPENSE" or "DELETE_INCOME" with transaction ID if known or description. The app will show confirmation to the user before deleting.
4. Keep answers friendly, objective, helpful, and financial-focused."""
    }

    val aiSettingsFlow: Flow<AiSettings> = context.dataStore.data.map { prefs ->
        AiSettings(
            openRouterApiKey = prefs[KEY_OPENROUTER_API_KEY] ?: try { BuildConfig.OPENROUTER_API_KEY } catch (e: Throwable) { "" },
            geminiApiKey = prefs[KEY_GEMINI_API_KEY] ?: try { BuildConfig.GEMINI_API_KEY } catch (e: Throwable) { "" },
            primaryProvider = prefs[KEY_PRIMARY_PROVIDER] ?: "OpenRouter",
            fallbackProvider = prefs[KEY_FALLBACK_PROVIDER] ?: "Gemini",
            openRouterModel = prefs[KEY_OPENROUTER_MODEL] ?: "google/gemini-2.5-flash",
            geminiModel = prefs[KEY_GEMINI_MODEL] ?: "gemini-3.5-flash",
            temperature = prefs[KEY_TEMPERATURE] ?: 0.7f,
            maxTokens = prefs[KEY_MAX_TOKENS] ?: 2048,
            autoFailover = prefs[KEY_AUTO_FAILOVER] ?: true,
            aiEnabled = prefs[KEY_AI_ENABLED] ?: true,
            systemPrompt = prefs[KEY_SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
        )
    }

    suspend fun updateSettings(
        openRouterKey: String,
        geminiKey: String,
        primary: String,
        fallback: String,
        openRouterModel: String,
        geminiModel: String,
        temperature: Float,
        maxTokens: Int,
        autoFailover: Boolean,
        aiEnabled: Boolean,
        systemPrompt: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPENROUTER_API_KEY] = openRouterKey
            prefs[KEY_GEMINI_API_KEY] = geminiKey
            prefs[KEY_PRIMARY_PROVIDER] = primary
            prefs[KEY_FALLBACK_PROVIDER] = fallback
            prefs[KEY_OPENROUTER_MODEL] = openRouterModel
            prefs[KEY_GEMINI_MODEL] = geminiModel
            prefs[KEY_TEMPERATURE] = temperature
            prefs[KEY_MAX_TOKENS] = maxTokens
            prefs[KEY_AUTO_FAILOVER] = autoFailover
            prefs[KEY_AI_ENABLED] = aiEnabled
            prefs[KEY_SYSTEM_PROMPT] = systemPrompt
        }
    }
}
