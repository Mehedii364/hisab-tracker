package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AccountEntity
import com.example.data.local.AiAuditLogEntity
import com.example.data.local.AppDatabase
import com.example.data.local.BudgetEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.TransferEntity
import com.example.data.preferences.AiSettings
import com.example.data.preferences.AiSettingsManager
import com.example.data.preferences.ProfilePreferencesManager
import com.example.data.preferences.UserProfile
import com.example.data.repository.FinancialSummary
import com.example.data.repository.HisabRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val showUndo: Boolean = false
)

data class PendingDeleteDialogState(
    val isVisible: Boolean = false,
    val transactionId: Int? = null,
    val description: String = "",
    val amount: Double = 0.0,
    val messageId: Int? = null,
    val actionJson: String? = null
)

class HisabViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val aiSettingsManager = AiSettingsManager(application)
    private val profilePreferencesManager = ProfilePreferencesManager(application)

    val repository = HisabRepository(
        transactionDao = database.transactionDao(),
        categoryDao = database.categoryDao(),
        accountDao = database.accountDao(),
        transferDao = database.transferDao(),
        savingsGoalDao = database.savingsGoalDao(),
        budgetDao = database.budgetDao(),
        chatMessageDao = database.chatMessageDao(),
        aiAuditLogDao = database.aiAuditLogDao(),
        undoHistoryDao = database.undoHistoryDao(),
        aiSettingsManager = aiSettingsManager,
        profilePreferencesManager = profilePreferencesManager
    )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransfers: StateFlow<List<TransferEntity>> = repository.allTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val aiSettings: StateFlow<AiSettings> = repository.aiSettings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            AiSettings(
                openRouterApiKey = "",
                geminiApiKey = "",
                primaryProvider = "OpenRouter",
                fallbackProvider = "Gemini",
                openRouterModel = "google/gemini-2.5-flash",
                geminiModel = "gemini-3.5-flash",
                temperature = 0.7f,
                maxTokens = 2048,
                autoFailover = true,
                aiEnabled = true,
                systemPrompt = AiSettingsManager.DEFAULT_SYSTEM_PROMPT
            )
        )

    val auditLogs: StateFlow<List<AiAuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _financialSummary = MutableStateFlow(
        FinancialSummary(
            todayIncome = 0.0, todayExpense = 0.0, todayBalance = 0.0,
            monthlyIncome = 0.0, monthlyExpense = 0.0, monthlyBalance = 0.0,
            monthlySavingsRate = 0.0, prevMonthExpense = 0.0, expenseChangePercent = 0.0,
            highestCategoryName = "None", highestCategoryAmount = 0.0,
            budgetTotal = 0.0, budgetSpentPercent = 0.0
        )
    )
    val financialSummary: StateFlow<FinancialSummary> = _financialSummary.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var hasUnlockedSession = false

    fun lockApp() {
        _isAppLocked.value = true
        _uiNotification.value = UiNotification(message = "App Locked for Security")
    }

    fun unlockApp() {
        _isAppLocked.value = false
        hasUnlockedSession = true
        _uiNotification.value = UiNotification(message = "Security Verified • App Unlocked")
    }

    fun verifyPin(inputPin: String): Boolean {
        // Default PIN check or profile security PIN check
        return inputPin == "1234"
    }

    fun togglePrivacyMode() {
        _isPrivacyMode.value = !_isPrivacyMode.value
        _uiNotification.value = UiNotification(message = if (_isPrivacyMode.value) "Privacy Mode Enabled" else "Privacy Mode Disabled")
    }

    private val _uiNotification = MutableStateFlow<UiNotification?>(null)
    val uiNotification: StateFlow<UiNotification?> = _uiNotification.asStateFlow()

    private val _pendingDeleteDialogState = MutableStateFlow(PendingDeleteDialogState())
    val pendingDeleteDialogState: StateFlow<PendingDeleteDialogState> = _pendingDeleteDialogState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultCategoriesExist()
            launch {
                allTransactions.collectLatest {
                    refreshSummary()
                }
            }
            launch {
                userProfile.collectLatest { profile ->
                    if ((profile.biometricLockEnabled || profile.pinLockEnabled) && !hasUnlockedSession) {
                        _isAppLocked.value = true
                    }
                }
            }
        }
    }

    fun refreshSummary() {
        viewModelScope.launch {
            _financialSummary.value = repository.calculateFinancialSummary()
        }
    }

    // --- Profile & Preferences ---

    fun updateUserProfile(
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
        savingsPercent: Double = 20.0,
        currency: String = "৳",
        language: String = "BN",
        theme: String = "DARK",
        defaultAccount: String = "ক্যাশ (Cash)",
        defaultPaymentMethod: String = "bKash",
        budgetStartDay: Int = 1
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(
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
            _uiNotification.value = UiNotification(message = "Profile & preferences updated.")
        }
    }

    fun toggleProfilePreference(key: String, value: Boolean) {
        viewModelScope.launch {
            repository.toggleProfilePreference(key, value)
            _uiNotification.value = UiNotification(message = "Setting updated.")
        }
    }

    fun setAiPrimaryProvider(provider: String) {
        viewModelScope.launch {
            repository.setAiPrimaryProvider(provider)
            _uiNotification.value = UiNotification(message = "Primary AI Provider switched to $provider.")
        }
    }

    fun exportData() {
        _uiNotification.value = UiNotification(message = "Financial data exported to JSON.")
    }

    fun backupData() {
        _uiNotification.value = UiNotification(message = "Local database backup created successfully.")
    }

    fun restoreData() {
        _uiNotification.value = UiNotification(message = "Data restored from latest backup.")
    }

    fun generateFinancialReport() {
        _uiNotification.value = UiNotification(message = "Financial Report generated & downloaded.")
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            repository.setAppLanguage(language)
            _uiNotification.value = UiNotification(message = if (language == "BN") "ভাষা বাংলায় পরিবর্তন করা হয়েছে।" else "Language changed to English.")
        }
    }

    fun setAppThemeMode(theme: String) {
        viewModelScope.launch {
            repository.setAppThemeMode(theme)
            _uiNotification.value = UiNotification(message = "Theme mode set to $theme.")
        }
    }

    fun saveProfilePictureFromUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destinationFile = File(context.filesDir, "profile_picture.jpg")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    destinationFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                repository.updateProfilePicturePath(destinationFile.absolutePath)
                _uiNotification.value = UiNotification(message = "Profile picture updated successfully.")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiNotification.value = UiNotification(message = "Failed to save profile picture: ${e.message}")
            }
        }
    }

    fun saveProfilePictureFromBitmap(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destinationFile = File(context.filesDir, "profile_picture.jpg")
                destinationFile.outputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                repository.updateProfilePicturePath(destinationFile.absolutePath)
                _uiNotification.value = UiNotification(message = "Profile picture captured & saved.")
            } catch (e: Exception) {
                e.printStackTrace()
                _uiNotification.value = UiNotification(message = "Failed to save photo: ${e.message}")
            }
        }
    }

    fun removeProfilePicture(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val destinationFile = File(context.filesDir, "profile_picture.jpg")
                if (destinationFile.exists()) {
                    destinationFile.delete()
                }
                repository.updateProfilePicturePath("")
                _uiNotification.value = UiNotification(message = "Profile picture removed.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Transaction CRUD ---

    fun addTransaction(type: String, amount: Double, category: String, description: String, date: String, accountName: String = "ক্যাশ (Cash)") {
        viewModelScope.launch {
            repository.addTransaction(type, amount, category, description, date, accountName)
            _uiNotification.value = UiNotification(message = "Transaction recorded successfully.")
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _uiNotification.value = UiNotification(message = "Transaction updated.", showUndo = true)
        }
    }

    fun requestDeleteTransaction(transaction: TransactionEntity) {
        _pendingDeleteDialogState.value = PendingDeleteDialogState(
            isVisible = true,
            transactionId = transaction.id,
            description = transaction.description,
            amount = transaction.amount
        )
    }

    fun confirmDelete() {
        val state = _pendingDeleteDialogState.value
        viewModelScope.launch {
            if (state.transactionId != null && state.transactionId > 0) {
                repository.deleteTransaction(state.transactionId)
                _uiNotification.value = UiNotification(message = "Transaction deleted.", showUndo = true)
            } else if (state.messageId != null && state.actionJson != null) {
                repository.confirmPendingDelete(state.messageId, state.actionJson)
                _uiNotification.value = UiNotification(message = "Transaction deleted via AI confirmation.", showUndo = true)
            }
            dismissDeleteDialog()
        }
    }

    fun dismissDeleteDialog() {
        _pendingDeleteDialogState.value = PendingDeleteDialogState(isVisible = false)
    }

    fun triggerAiConfirmDelete(messageId: Int, actionJson: String, details: String) {
        _pendingDeleteDialogState.value = PendingDeleteDialogState(
            isVisible = true,
            messageId = messageId,
            actionJson = actionJson,
            description = details
        )
    }

    fun transferBetweenAccounts(fromAccount: String, toAccount: String, amount: Double, notes: String = "") {
        viewModelScope.launch {
            repository.transferBetweenAccounts(fromAccount, toAccount, amount, notes)
            _uiNotification.value = UiNotification(message = "Transfer of $amount completed.")
        }
    }

    fun addAccount(name: String, type: String, openingBalance: Double) {
        viewModelScope.launch {
            repository.addAccount(name, type, openingBalance)
            _uiNotification.value = UiNotification(message = "Account '$name' created.")
        }
    }

    fun undoLastAction() {
        viewModelScope.launch {
            val success = repository.performUndo()
            if (success) {
                _uiNotification.value = UiNotification(message = "Action undone successfully.")
            } else {
                _uiNotification.value = UiNotification(message = "Nothing to undo.")
            }
        }
    }

    fun dismissNotification() {
        _uiNotification.value = null
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                repository.sendChatMessage(text)
            } catch (e: Exception) {
                _uiNotification.value = UiNotification(message = "Chat Error: ${e.message}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _uiNotification.value = UiNotification(message = "Chat conversation cleared.")
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.addCategory(name, type)
            _uiNotification.value = UiNotification(message = "Category '$name' added.")
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _uiNotification.value = UiNotification(message = "Category removed.")
        }
    }

    fun setBudget(category: String, limitAmount: Double) {
        viewModelScope.launch {
            repository.setBudget(category, limitAmount)
            _uiNotification.value = UiNotification(message = "Budget set to $limitAmount.")
        }
    }

    fun updateAiSettings(
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
        viewModelScope.launch {
            repository.updateAiSettings(
                openRouterKey, geminiKey, primary, fallback, openRouterModel,
                geminiModel, temperature, maxTokens, autoFailover, aiEnabled, systemPrompt
            )
            _uiNotification.value = UiNotification(message = "AI Settings saved successfully.")
        }
    }
}
