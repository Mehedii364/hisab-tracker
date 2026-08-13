package com.example.data.ai

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

data class StructuredAiAction(
    val action: String, // CREATE_EXPENSE, CREATE_INCOME, UPDATE_EXPENSE, DELETE_EXPENSE, SEARCH_TRANSACTIONS, CREATE_CATEGORY, CREATE_BUDGET, GENERATE_REPORT, ANALYZE_FINANCES
    val amount: Double? = null,
    val category: String? = null,
    val description: String? = null,
    val date: String? = null, // YYYY-MM-DD
    val id: Int? = null,
    val query: String? = null,
    val type: String? = null // "INCOME" or "EXPENSE"
)

object AiActionParser {

    /**
     * Extracts JSON action block from AI response text if present.
     * Looks for ```json { ... } ``` or raw JSON object containing "action".
     */
    fun parseAction(responseText: String): StructuredAiAction? {
        try {
            var jsonString: String? = null

            // Try codeblock format first
            val jsonBlockRegex = """```json\s*(\{[\s\S]*?\})\s*```""".toRegex()
            val match = jsonBlockRegex.find(responseText)
            if (match != null) {
                jsonString = match.groupValues[1]
            } else {
                // Try finding raw JSON object
                val rawJsonRegex = """(\{\s*"action"\s*:\s*"[A-Z_]+"[^}]*\})""".toRegex()
                val rawMatch = rawJsonRegex.find(responseText)
                if (rawMatch != null) {
                    jsonString = rawMatch.groupValues[1]
                }
            }

            if (jsonString == null) return null

            val jsonObject = JSONObject(jsonString)
            if (!jsonObject.has("action")) return null

            val actionName = jsonObject.getString("action")
            val amount = if (jsonObject.has("amount") && !jsonObject.isNull("amount")) jsonObject.getDouble("amount") else null
            val category = if (jsonObject.has("category") && !jsonObject.isNull("category")) jsonObject.getString("category") else null
            val description = if (jsonObject.has("description") && !jsonObject.isNull("description")) jsonObject.getString("description") else null
            val date = if (jsonObject.has("date") && !jsonObject.isNull("date")) jsonObject.getString("date") else null
            val id = if (jsonObject.has("id") && !jsonObject.isNull("id")) jsonObject.getInt("id") else null
            val query = if (jsonObject.has("query") && !jsonObject.isNull("query")) jsonObject.getString("query") else null
            val type = if (jsonObject.has("type") && !jsonObject.isNull("type")) jsonObject.getString("type") else null

            return StructuredAiAction(
                action = actionName,
                amount = amount,
                category = category,
                description = description,
                date = date,
                id = id,
                query = query,
                type = type
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Cleans response text by stripping out raw json action block if desired or formatting cleanly.
     */
    fun cleanResponseText(responseText: String): String {
        val jsonBlockRegex = """```json\s*(\{[\s\S]*?\})\s*```""".toRegex()
        return responseText.replace(jsonBlockRegex, "").trim()
    }
}
