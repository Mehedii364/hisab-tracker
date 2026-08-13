package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val subcategory: String = "",
    val description: String,
    val date: String, // YYYY-MM-DD
    val accountName: String = "Cash",
    val paymentMethod: String = "Cash",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val iconName: String = "Category",
    val colorHex: Long = 0xFF3B82F6
)

@Entity(tableName = "subcategories")
data class SubcategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val categoryName: String,
    val name: String,
    val iconName: String = "SubCategory"
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g., "bKash Wallet", "Bank Account", "Cash"
    val type: String, // "CASH", "BANK", "MOBILE_BANKING", "CARD"
    val balance: Double = 0.0,
    val accountNumber: String = "",
    val colorHex: Long = 0xFF10B981,
    val status: String = "ACTIVE"
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fromAccount: String,
    val toAccount: String,
    val amount: Double,
    val date: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalName: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadlineDate: String = "",
    val colorHex: Long = 0xFF6366F1
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val amount: Double,
    val category: String,
    val description: String,
    val frequency: String = "MONTHLY", // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val accountName: String = "Cash",
    val isEnabled: Boolean = true
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "TOTAL" or category name
    val limitAmount: Double,
    val monthYear: String // YYYY-MM
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String = "default_session",
    val sender: String, // "USER" or "AI"
    val message: String,
    val providerUsed: String = "System", // "OpenRouter", "Gemini", "System"
    val timestamp: Long = System.currentTimeMillis(),
    val actionExecuted: String? = null,
    val pendingActionJson: String? = null,
    val requiresConfirmation: Boolean = false
)

@Entity(tableName = "ai_audit_logs")
data class AiAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String,
    val transactionId: Int? = null,
    val provider: String,
    val model: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "SUCCESS" or "FAILED"
    val details: String
)

@Entity(tableName = "undo_history")
data class UndoHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityType: String, // "TRANSACTION", "CATEGORY", "BUDGET", "TRANSFER"
    val actionType: String, // "DELETE", "UPDATE"
    val serializedOriginalData: String,
    val timestamp: Long = System.currentTimeMillis()
)
