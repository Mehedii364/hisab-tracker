package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        SubcategoryEntity::class,
        AccountEntity::class,
        TransferEntity::class,
        SavingsGoalEntity::class,
        RecurringTransactionEntity::class,
        BudgetEntity::class,
        ChatMessageEntity::class,
        AiAuditLogEntity::class,
        UndoHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiAuditLogDao(): AiAuditLogDao
    abstract fun undoHistoryDao(): UndoHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisab_tracker_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateInitialDefaults(database)
                    }
                }
            }

            suspend fun populateInitialDefaults(database: AppDatabase) {
                // Populate default accounts
                val defaultAccounts = listOf(
                    AccountEntity(name = "ক্যাশ (Cash)", type = "CASH", balance = 5000.0, colorHex = 0xFF10B981),
                    AccountEntity(name = "ব্যাংক (Bank Account)", type = "BANK", balance = 25000.0, colorHex = 0xFF3B82F6),
                    AccountEntity(name = "বিকাশ (bKash)", type = "MOBILE_BANKING", balance = 8500.0, colorHex = 0xFFEC4899),
                    AccountEntity(name = "নগদ (Nagad)", type = "MOBILE_BANKING", balance = 3000.0, colorHex = 0xFFF59E0B),
                    AccountEntity(name = "রকেট (Rocket)", type = "MOBILE_BANKING", balance = 1500.0, colorHex = 0xFF8B5CF6)
                )
                for (acc in defaultAccounts) {
                    database.accountDao().insertAccount(acc)
                }

                // Populate Bangla + English default Categories
                val defaultCategories = listOf(
                    CategoryEntity(name = "খাবার (Food & Dining)", type = "EXPENSE", iconName = "Restaurant", colorHex = 0xFFEF4444),
                    CategoryEntity(name = "বাসা (Housing & Bills)", type = "EXPENSE", iconName = "Home", colorHex = 0xFF10B981),
                    CategoryEntity(name = "যাতায়াত (Transportation)", type = "EXPENSE", iconName = "DirectionsCar", colorHex = 0xFF3B82F6),
                    CategoryEntity(name = "পরিবার ও সন্তান (Family)", type = "EXPENSE", iconName = "Group", colorHex = 0xFFF59E0B),
                    CategoryEntity(name = "স্বাস্থ্য (Health)", type = "EXPENSE", iconName = "MedicalServices", colorHex = 0xFFEC4899),
                    CategoryEntity(name = "শিক্ষা (Education)", type = "EXPENSE", iconName = "School", colorHex = 0xFF6366F1),
                    CategoryEntity(name = "প্রযুক্তি (Tech & Internet)", type = "EXPENSE", iconName = "Devices", colorHex = 0xFF0EA5E9),
                    CategoryEntity(name = "বিনোদোন ও ভ্রমণ (Entertainment)", type = "EXPENSE", iconName = "Movie", colorHex = 0xFF8B5CF6),
                    CategoryEntity(name = "চাকরি (Salary)", type = "INCOME", iconName = "AttachMoney", colorHex = 0xFF10B981),
                    CategoryEntity(name = "ফ্রিল্যান্সিং (Freelancing)", type = "INCOME", iconName = "Laptop", colorHex = 0xFF6366F1),
                    CategoryEntity(name = "ব্যবসা (Business)", type = "INCOME", iconName = "Store", colorHex = 0xFFF59E0B),
                    CategoryEntity(name = "বিনিয়োগ (Investments)", type = "INCOME", iconName = "TrendingUp", colorHex = 0xFF14B8A6)
                )
                for (cat in defaultCategories) {
                    database.categoryDao().insertCategory(cat)
                }

                // Default Savings Goal
                database.savingsGoalDao().insertSavingsGoal(
                    SavingsGoalEntity(goalName = "জরুরি তহবিল (Emergency Fund)", targetAmount = 50000.0, currentAmount = 18500.0, deadlineDate = "2026-12-31")
                )
            }
        }
    }
}
