package com.example.data.repository

import com.example.data.ai.AiActionParser
import com.example.data.ai.AiProviderManager
import com.example.data.ai.AiResponse
import com.example.data.ai.StructuredAiAction
import com.example.data.local.AccountDao
import com.example.data.local.AccountEntity
import com.example.data.local.AiAuditLogDao
import com.example.data.local.AiAuditLogEntity
import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.SavingsGoalDao
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.TransferDao
import com.example.data.local.TransferEntity
import com.example.data.local.UndoHistoryDao
import com.example.data.local.UndoHistoryEntity
import com.example.data.preferences.AiSettings
import com.example.data.preferences.AiSettingsManager
import com.example.data.preferences.ProfilePreferencesManager
import com.example.data.preferences.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinancialSummary(
    val todayIncome: Double,
    val todayExpense: Double,
    val todayBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val monthlyBalance: Double,
    val monthlySavingsRate: Double,
    val prevMonthExpense: Double,
    val expenseChangePercent: Double,
    val highestCategoryName: String,
    val highestCategoryAmount: Double,
    val budgetTotal: Double,
    val budgetSpentPercent: Double
)

class HisabRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val transferDao: TransferDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val budgetDao: BudgetDao,
    private val chatMessageDao: ChatMessageDao,
    private val aiAuditLogDao: AiAuditLogDao,
    private val undoHistoryDao: UndoHistoryDao,
    private val aiSettingsManager: AiSettingsManager,
    private val profilePreferencesManager: ProfilePreferencesManager
) {
    private val aiProviderManager = AiProviderManager()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val transactionAdapter = moshi.adapter(TransactionEntity::class.java)

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allTransfers: Flow<List<TransferEntity>> = transferDao.getAllTransfers()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllSavingsGoals()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getChatMessages()
    val aiSettings: Flow<AiSettings> = aiSettingsManager.aiSettingsFlow
    val userProfile: Flow<UserProfile> = profilePreferencesManager.userProfileFlow
    val auditLogs: Flow<List<AiAuditLogEntity>> = aiAuditLogDao.getAuditLogs()

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(monthYear)

    // --- Profile & Preferences ---

    suspend fun updateUserProfile(
        name: String,
        nickname: String = "Mehedi",
        mobileNumber: String = "+880 1712-345678",
        email: String,
        address: String = "Dhaka, Bangladesh",
        country: String = "Bangladesh",
        timezone: String = "GMT+6 (Asia/Dhaka)",
        bio: String = "Hisab Tracker Premium User",
        incomeTarget: Double,
        expenseLimit: Double = 35000.0,
        savingsTarget: Double = 15000.0,
        savingsPercent: Double,
        currency: String,
        language: String,
        theme: String,
        defaultAccount: String = "ক্যাশ (Cash)",
        defaultPaymentMethod: String = "bKash",
        budgetStartDay: Int = 1
    ) = withContext(Dispatchers.IO) {
        profilePreferencesManager.updateProfile(
            name = name,
            nickname = nickname,
            mobileNumber = mobileNumber,
            email = email,
            address = address,
            country = country,
            timezone = timezone,
            bio = bio,
            incomeTarget = incomeTarget,
            expenseLimit = expenseLimit,
            savingsTarget = savingsTarget,
            savingsPercent = savingsPercent,
            currency = currency,
            language = language,
            theme = theme,
            defaultAccount = defaultAccount,
            defaultPaymentMethod = defaultPaymentMethod,
            budgetStartDay = budgetStartDay
        )
    }

    suspend fun setAppLanguage(language: String) = withContext(Dispatchers.IO) {
        profilePreferencesManager.setLanguage(language)
    }

    suspend fun setAppThemeMode(theme: String) = withContext(Dispatchers.IO) {
        profilePreferencesManager.setThemeMode(theme)
    }

    suspend fun updateProfilePicturePath(path: String) = withContext(Dispatchers.IO) {
        profilePreferencesManager.setProfilePicturePath(path)
    }

    suspend fun toggleProfilePreference(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        profilePreferencesManager.updateTogglePreference(key, value)
    }

    suspend fun setAiPrimaryProvider(provider: String) = withContext(Dispatchers.IO) {
        profilePreferencesManager.updateAiProvider(provider)
    }

    // --- Transaction CRUD & Account Balance Sync ---

    suspend fun addTransaction(
        type: String,
        amount: Double,
        category: String,
        description: String,
        date: String,
        accountName: String = "ক্যাশ (Cash)"
    ): Long = withContext(Dispatchers.IO) {
        val detectedCategory = if (category.isBlank() || category.equals("General", true)) {
            autoDetectCategory(description, type)
        } else {
            category
        }

        val tx = TransactionEntity(
            type = type.uppercase(),
            amount = amount,
            category = detectedCategory,
            description = description,
            date = if (date.isBlank()) getCurrentDate() else date,
            accountName = accountName
        )
        val id = transactionDao.insertTransaction(tx)

        // Update corresponding account balance
        val delta = if (type.equals("INCOME", true)) amount else -amount
        accountDao.updateAccountBalance(accountName, delta)

        id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val original = transactionDao.getTransactionById(transaction.id)
        if (original != null) {
            saveUndoState("TRANSACTION", "UPDATE", transactionAdapter.toJson(original))
        }
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) = withContext(Dispatchers.IO) {
        val original = transactionDao.getTransactionById(id)
        if (original != null) {
            saveUndoState("TRANSACTION", "DELETE", transactionAdapter.toJson(original))
            transactionDao.deleteTransactionById(id)
        }
    }

    suspend fun searchTransactions(query: String): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.searchTransactions(query)
    }

    // --- Account Transfers ---

    suspend fun transferBetweenAccounts(fromAccount: String, toAccount: String, amount: Double, notes: String = "") = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext
        accountDao.updateAccountBalance(fromAccount, -amount)
        accountDao.updateAccountBalance(toAccount, amount)

        transferDao.insertTransfer(
            TransferEntity(
                fromAccount = fromAccount,
                toAccount = toAccount,
                amount = amount,
                date = getCurrentDate(),
                description = notes
            )
        )
    }

    suspend fun addAccount(name: String, type: String, openingBalance: Double) = withContext(Dispatchers.IO) {
        accountDao.insertAccount(
            AccountEntity(name = name, type = type, balance = openingBalance)
        )
    }

    // --- Automatic Bangla/English Category Detection ---

    private fun autoDetectCategory(description: String, type: String): String {
        val text = description.lowercase()
        return if (type.equals("INCOME", true)) {
            when {
                text.contains("salary") || text.contains("বেতন") || text.contains("bonus") || text.contains("বোনাস") -> "চাকরি (Salary)"
                text.contains("freelance") || text.contains("উপওয়ার্ক") || text.contains("fiverr") || text.contains("ফ্রিল্যান্সিং") -> "ফ্রিল্যান্সিং (Freelancing)"
                text.contains("business") || text.contains("ব্যবসা") || text.contains("বিক্রি") -> "ব্যবসা (Business)"
                text.contains("profit") || text.contains("ডিভিডেন্ড") || text.contains("সুদ") -> "বিনিয়োগ (Investments)"
                else -> "অন্যান্য আয় (Other Income)"
            }
        } else {
            when {
                text.contains("খাবার") || text.contains("food") || text.contains("রেস্টুরেন্ট") || text.contains("নাস্তা") || text.contains("বিরিয়ানি") -> "খাবার (Food & Dining)"
                text.contains("ভাড়া") || text.contains("rent") || text.contains("বিদ্যুৎ") || text.contains("গ্যাস") || text.contains("ইন্টারনেট") -> "বাসা (Housing & Bills)"
                text.contains("bus") || text.contains("বাস") || text.contains("pathao") || text.contains("uber") || text.contains("রিকশা") || text.contains("cng") || text.contains("যাতায়াত") -> "যাতায়াত (Transportation)"
                text.contains("স্কুল") || text.contains("school") || text.contains("বই") || text.contains("কোচিং") -> "শিক্ষা (Education)"
                text.contains("ডাক্তার") || text.contains("doctor") || text.contains("ওষুধ") || text.contains("মেডিসিন") -> "স্বাস্থ্য (Health)"
                text.contains("মোবাইল") || text.contains("recharge") || text.contains("রিচার্জ") || text.contains("wifi") -> "প্রযুক্তি (Tech & Internet)"
                else -> "অন্যান্য ব্যয় (Other Expense)"
            }
        }
    }

    // --- Undo System ---

    private suspend fun saveUndoState(entityType: String, actionType: String, serializedData: String) {
        undoHistoryDao.insertUndoEntry(
            UndoHistoryEntity(
                entityType = entityType,
                actionType = actionType,
                serializedOriginalData = serializedData
            )
        )
    }

    suspend fun performUndo(): Boolean = withContext(Dispatchers.IO) {
        val lastUndo = undoHistoryDao.getLastUndoEntry() ?: return@withContext false
        try {
            if (lastUndo.entityType == "TRANSACTION") {
                val originalTx = transactionAdapter.fromJson(lastUndo.serializedOriginalData)
                if (originalTx != null) {
                    transactionDao.insertTransaction(originalTx)
                    undoHistoryDao.deleteUndoEntryById(lastUndo.id)
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext false
    }

    // --- Category & Budget CRUD ---

    suspend fun ensureDefaultCategoriesExist() = withContext(Dispatchers.IO) {
        val existing = categoryDao.getAllCategories().first()
        if (existing.isEmpty()) {
            val defaultCategories = listOf(
                CategoryEntity(name = "খাবার (Food & Dining)", type = "EXPENSE", iconName = "Restaurant", colorHex = 0xFFEF4444),
                CategoryEntity(name = "বাসা (Housing & Bills)", type = "EXPENSE", iconName = "Home", colorHex = 0xFF10B981),
                CategoryEntity(name = "যাতায়াত (Transportation)", type = "EXPENSE", iconName = "DirectionsCar", colorHex = 0xFF3B82F6),
                CategoryEntity(name = "পরিবার ও সন্তান (Family)", type = "EXPENSE", iconName = "Group", colorHex = 0xFFF59E0B),
                CategoryEntity(name = "স্বাস্থ্য (Health)", type = "EXPENSE", iconName = "MedicalServices", colorHex = 0xFFEC4899),
                CategoryEntity(name = "শিক্ষা (Education)", type = "EXPENSE", iconName = "School", colorHex = 0xFF6366F1),
                CategoryEntity(name = "প্রযুক্তি (Tech & Internet)", type = "EXPENSE", iconName = "Devices", colorHex = 0xFF0EA5E9),
                CategoryEntity(name = "চাকরি (Salary)", type = "INCOME", iconName = "AttachMoney", colorHex = 0xFF10B981),
                CategoryEntity(name = "ফ্রিল্যান্সিং (Freelancing)", type = "INCOME", iconName = "Laptop", colorHex = 0xFF6366F1),
                CategoryEntity(name = "ব্যবসা (Business)", type = "INCOME", iconName = "Store", colorHex = 0xFFF59E0B)
            )
            for (cat in defaultCategories) {
                categoryDao.insertCategory(cat)
            }
        }
    }

    suspend fun addCategory(name: String, type: String, colorHex: Long = 0xFF3B82F6) = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(CategoryEntity(name = name, type = type.uppercase(), colorHex = colorHex))
    }

    suspend fun deleteCategory(id: Int) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun setBudget(category: String, limitAmount: Double, monthYear: String = getCurrentMonthYear()) = withContext(Dispatchers.IO) {
        budgetDao.insertOrUpdateBudget(
            BudgetEntity(category = category, limitAmount = limitAmount, monthYear = monthYear)
        )
    }

    // --- Financial Calculations & Analytics ---

    suspend fun calculateFinancialSummary(): FinancialSummary = withContext(Dispatchers.IO) {
        val transactions = transactionDao.getAllTransactions().first()
        val today = getCurrentDate()
        val currentMonthYear = getCurrentMonthYear()
        val prevMonthYear = getPreviousMonthYear()

        var todayInc = 0.0
        var todayExp = 0.0
        var monthInc = 0.0
        var monthExp = 0.0
        var prevMonthExp = 0.0

        val categoryExpenseMap = mutableMapOf<String, Double>()

        for (tx in transactions) {
            if (tx.date == today) {
                if (tx.type == "INCOME") todayInc += tx.amount else todayExp += tx.amount
            }

            if (tx.date.startsWith(currentMonthYear)) {
                if (tx.type == "INCOME") {
                    monthInc += tx.amount
                } else {
                    monthExp += tx.amount
                    categoryExpenseMap[tx.category] = (categoryExpenseMap[tx.category] ?: 0.0) + tx.amount
                }
            }

            if (tx.date.startsWith(prevMonthYear)) {
                if (tx.type == "EXPENSE") {
                    prevMonthExp += tx.amount
                }
            }
        }

        val highestCategory = categoryExpenseMap.maxByOrNull { it.value }
        val highestCategoryName = highestCategory?.key ?: "None"
        val highestCategoryAmount = highestCategory?.value ?: 0.0

        val budgets = budgetDao.getBudgetsForMonth(currentMonthYear).first()
        val totalBudget = budgets.find { it.category == "TOTAL" }?.limitAmount ?: budgets.sumOf { it.limitAmount }
        val budgetSpentPercent = if (totalBudget > 0) (monthExp / totalBudget) * 100 else 0.0

        val expenseChangePercent = if (prevMonthExp > 0) ((monthExp - prevMonthExp) / prevMonthExp) * 100 else 0.0
        val savingsRate = if (monthInc > 0) ((monthInc - monthExp) / monthInc) * 100 else 0.0

        FinancialSummary(
            todayIncome = todayInc,
            todayExpense = todayExp,
            todayBalance = todayInc - todayExp,
            monthlyIncome = monthInc,
            monthlyExpense = monthExp,
            monthlyBalance = monthInc - monthExp,
            monthlySavingsRate = savingsRate.coerceAtLeast(0.0),
            prevMonthExpense = prevMonthExp,
            expenseChangePercent = expenseChangePercent,
            highestCategoryName = highestCategoryName,
            highestCategoryAmount = highestCategoryAmount,
            budgetTotal = totalBudget,
            budgetSpentPercent = budgetSpentPercent.coerceAtMost(100.0)
        )
    }

    // --- AI Chat & Action Execution System ---

    suspend fun sendChatMessage(userText: String): String = withContext(Dispatchers.IO) {
        val settings = aiSettings.first()

        // 1. Save user message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                sender = "USER",
                message = userText,
                providerUsed = "User"
            )
        )

        // 2. Fetch conversation history
        val historyList = chatMessageDao.getChatMessages().first().map {
            Pair(it.sender, it.message)
        }

        // 3. Process with AI Provider Manager (OpenRouter / Gemini + Auto Failover)
        val aiResponse = aiProviderManager.processChatRequest(userText, historyList, settings)

        // 4. Handle Structured AI Action if present
        var actionSummary = ""
        var pendingActionJson: String? = null
        var requiresConfirmation = false

        val action = aiResponse.action
        if (action != null) {
            val executionResult = handleAiAction(action)
            actionSummary = executionResult.summary
            pendingActionJson = executionResult.pendingActionJson
            requiresConfirmation = executionResult.requiresConfirmation

            // Log AI Audit
            aiAuditLogDao.insertAuditLog(
                AiAuditLogEntity(
                    action = action.action,
                    provider = aiResponse.providerUsed,
                    model = aiResponse.modelUsed,
                    status = if (executionResult.success) "SUCCESS" else "FAILED",
                    details = "Prompt: $userText | Result: ${executionResult.summary}"
                )
            )
        }

        val finalAiMessage = AiActionParser.cleanResponseText(aiResponse.text) +
                if (actionSummary.isNotBlank()) "\n\n$actionSummary" else ""

        // 5. Save AI response message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                sender = "AI",
                message = finalAiMessage,
                providerUsed = aiResponse.providerUsed,
                actionExecuted = action?.action,
                pendingActionJson = pendingActionJson,
                requiresConfirmation = requiresConfirmation
            )
        )

        return@withContext finalAiMessage
    }

    data class ActionExecutionResult(
        val success: Boolean,
        val summary: String,
        val requiresConfirmation: Boolean = false,
        val pendingActionJson: String? = null
    )

    private suspend fun handleAiAction(action: StructuredAiAction): ActionExecutionResult {
        return try {
            when (action.action) {
                "CREATE_EXPENSE", "CREATE_INCOME" -> {
                    val amount = action.amount ?: 0.0
                    val category = action.category ?: ""
                    val desc = action.description ?: "Added via AI"
                    val date = action.date ?: getCurrentDate()
                    val type = if (action.action == "CREATE_INCOME") "INCOME" else "EXPENSE"

                    addTransaction(type, amount, category, desc, date)
                    ActionExecutionResult(
                        success = true,
                        summary = "✅ Added $type of ৳$amount for '$desc' on $date."
                    )
                }

                "DELETE_EXPENSE", "DELETE_INCOME" -> {
                    val jsonString = JSONObject().apply {
                        put("action", action.action)
                        put("id", action.id ?: 0)
                        put("category", action.category ?: "")
                        put("amount", action.amount ?: 0.0)
                        put("description", action.description ?: "")
                    }.toString()

                    ActionExecutionResult(
                        success = true,
                        summary = "⚠️ Deletion requested. Please confirm deletion below.",
                        requiresConfirmation = true,
                        pendingActionJson = jsonString
                    )
                }

                "CREATE_CATEGORY" -> {
                    val name = action.category ?: action.query ?: "New Category"
                    val type = action.type ?: "EXPENSE"
                    addCategory(name, type)
                    ActionExecutionResult(
                        success = true,
                        summary = "✅ Created new category: '$name' ($type)."
                    )
                }

                "CREATE_BUDGET" -> {
                    val amount = action.amount ?: 0.0
                    val cat = action.category ?: "TOTAL"
                    setBudget(cat, amount)
                    ActionExecutionResult(
                        success = true,
                        summary = "✅ Set budget of ৳$amount for '$cat' this month."
                    )
                }

                "SEARCH_TRANSACTIONS" -> {
                    val q = action.query ?: action.category ?: ""
                    val results = searchTransactions(q)
                    val summaryText = if (results.isEmpty()) {
                        "🔍 No transactions found matching '$q'."
                    } else {
                        "🔍 Found ${results.size} matching transactions:\n" + results.take(5).joinToString("\n") {
                            "- ${it.date}: ৳${it.amount} [${it.category}] ${it.description}"
                        }
                    }
                    ActionExecutionResult(success = true, summary = summaryText)
                }

                else -> ActionExecutionResult(success = true, summary = "")
            }
        } catch (e: Exception) {
            ActionExecutionResult(success = false, summary = "❌ Action Failed: ${e.message}")
        }
    }

    suspend fun confirmPendingDelete(messageId: Int, actionJson: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(actionJson)
            val id = json.optInt("id", 0)
            if (id > 0) {
                deleteTransaction(id)
            } else {
                val amount = json.optDouble("amount", 0.0)
                val category = json.optString("category", "")
                val txs = transactionDao.getAllTransactions().first()
                val match = txs.find { (amount <= 0 || it.amount == amount) && (category.isBlank() || it.category.equals(category, true)) }
                if (match != null) {
                    deleteTransaction(match.id)
                }
            }
            chatMessageDao.clearPendingAction(messageId)
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearChatHistory()
    }

    suspend fun updateAiSettings(
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
        aiSettingsManager.updateSettings(
            openRouterKey, geminiKey, primary, fallback, openRouterModel,
            geminiModel, temperature, maxTokens, autoFailover, aiEnabled, systemPrompt
        )
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getPreviousMonthYear(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -1)
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(cal.time)
    }
}
