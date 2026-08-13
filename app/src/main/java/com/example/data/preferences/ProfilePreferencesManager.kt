package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "user_profile_prefs")

data class UserProfile(
    // Basic Information
    val userName: String = "Md. Mehedi Hasan",
    val nickname: String = "Mehedi",
    val mobileNumber: String = "+880 1712-345678",
    val userEmail: String = "mehedi@hisab.app",
    val address: String = "Dhaka, Bangladesh",
    val country: String = "Bangladesh",
    val currencySymbol: String = "৳",
    val timezone: String = "GMT+6 (Asia/Dhaka)",
    val bio: String = "Hisab Tracker Premium User • Smart Personal Finance",
    val profilePicturePath: String = "",

    // Financial Preferences
    val defaultAccount: String = "ক্যাশ (Cash)",
    val defaultPaymentMethod: String = "bKash",
    val monthlyIncomeTarget: Double = 50000.0,
    val monthlyExpenseLimit: Double = 35000.0,
    val monthlySavingsTarget: Double = 15000.0,
    val savingsTargetPercent: Double = 20.0,
    val budgetStartDay: Int = 1,

    // App Preferences
    val appLanguage: String = "BN", // "BN" (বাংলা) or "EN" (English)
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val notificationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val viewDensity: String = "COMFORTABLE", // "COMPACT" or "COMFORTABLE"
    val startPage: String = "HOME",

    // Security & AI
    val pinLockEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val aiEnabled: Boolean = true,
    val aiPrimaryProvider: String = "OPENROUTER", // "OPENROUTER" or "GEMINI"
    val aiAutoFallback: Boolean = true,
    val aiDefaultModel: String = "google/gemini-2.5-flash"
)

class ProfilePreferencesManager(private val context: Context) {

    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_NICKNAME = stringPreferencesKey("nickname")
        val KEY_MOBILE_NUMBER = stringPreferencesKey("mobile_number")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_ADDRESS = stringPreferencesKey("address")
        val KEY_COUNTRY = stringPreferencesKey("country")
        val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val KEY_TIMEZONE = stringPreferencesKey("timezone")
        val KEY_BIO = stringPreferencesKey("bio")
        val KEY_PROFILE_PICTURE_PATH = stringPreferencesKey("profile_picture_path")

        val KEY_DEFAULT_ACCOUNT = stringPreferencesKey("default_account")
        val KEY_DEFAULT_PAYMENT_METHOD = stringPreferencesKey("default_payment_method")
        val KEY_MONTHLY_INCOME_TARGET = doublePreferencesKey("monthly_income_target")
        val KEY_MONTHLY_EXPENSE_LIMIT = doublePreferencesKey("monthly_expense_limit")
        val KEY_MONTHLY_SAVINGS_TARGET = doublePreferencesKey("monthly_savings_target")
        val KEY_SAVINGS_TARGET_PERCENT = doublePreferencesKey("savings_target_percent")
        val KEY_BUDGET_START_DAY = intPreferencesKey("budget_start_day")

        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val KEY_VIEW_DENSITY = stringPreferencesKey("view_density")
        val KEY_START_PAGE = stringPreferencesKey("start_page")

        val KEY_PIN_LOCK_ENABLED = booleanPreferencesKey("pin_lock_enabled")
        val KEY_BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val KEY_AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val KEY_AI_PRIMARY_PROVIDER = stringPreferencesKey("ai_primary_provider")
        val KEY_AI_AUTO_FALLBACK = booleanPreferencesKey("ai_auto_fallback")
        val KEY_AI_DEFAULT_MODEL = stringPreferencesKey("ai_default_model")
    }

    val userProfileFlow: Flow<UserProfile> = context.profileDataStore.data.map { prefs ->
        UserProfile(
            userName = prefs[KEY_USER_NAME] ?: "Md. Mehedi Hasan",
            nickname = prefs[KEY_NICKNAME] ?: "Mehedi",
            mobileNumber = prefs[KEY_MOBILE_NUMBER] ?: "+880 1712-345678",
            userEmail = prefs[KEY_USER_EMAIL] ?: "mehedi@hisab.app",
            address = prefs[KEY_ADDRESS] ?: "Dhaka, Bangladesh",
            country = prefs[KEY_COUNTRY] ?: "Bangladesh",
            currencySymbol = prefs[KEY_CURRENCY_SYMBOL] ?: "৳",
            timezone = prefs[KEY_TIMEZONE] ?: "GMT+6 (Asia/Dhaka)",
            bio = prefs[KEY_BIO] ?: "Hisab Tracker Premium User • Smart Personal Finance",
            profilePicturePath = prefs[KEY_PROFILE_PICTURE_PATH] ?: "",

            defaultAccount = prefs[KEY_DEFAULT_ACCOUNT] ?: "ক্যাশ (Cash)",
            defaultPaymentMethod = prefs[KEY_DEFAULT_PAYMENT_METHOD] ?: "bKash",
            monthlyIncomeTarget = prefs[KEY_MONTHLY_INCOME_TARGET] ?: 50000.0,
            monthlyExpenseLimit = prefs[KEY_MONTHLY_EXPENSE_LIMIT] ?: 35000.0,
            monthlySavingsTarget = prefs[KEY_MONTHLY_SAVINGS_TARGET] ?: 15000.0,
            savingsTargetPercent = prefs[KEY_SAVINGS_TARGET_PERCENT] ?: 20.0,
            budgetStartDay = prefs[KEY_BUDGET_START_DAY] ?: 1,

            appLanguage = prefs[KEY_APP_LANGUAGE] ?: "BN",
            themeMode = prefs[KEY_THEME_MODE] ?: "DARK",
            notificationEnabled = prefs[KEY_NOTIFICATION_ENABLED] ?: true,
            soundEnabled = prefs[KEY_SOUND_ENABLED] ?: true,
            hapticEnabled = prefs[KEY_HAPTIC_ENABLED] ?: true,
            viewDensity = prefs[KEY_VIEW_DENSITY] ?: "COMFORTABLE",
            startPage = prefs[KEY_START_PAGE] ?: "HOME",

            pinLockEnabled = prefs[KEY_PIN_LOCK_ENABLED] ?: false,
            biometricLockEnabled = prefs[KEY_BIOMETRIC_LOCK_ENABLED] ?: false,
            aiEnabled = prefs[KEY_AI_ENABLED] ?: true,
            aiPrimaryProvider = prefs[KEY_AI_PRIMARY_PROVIDER] ?: "OPENROUTER",
            aiAutoFallback = prefs[KEY_AI_AUTO_FALLBACK] ?: true,
            aiDefaultModel = prefs[KEY_AI_DEFAULT_MODEL] ?: "google/gemini-2.5-flash"
        )
    }

    suspend fun updateProfile(
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
    ) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_NICKNAME] = nickname
            prefs[KEY_MOBILE_NUMBER] = mobileNumber
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_ADDRESS] = address
            prefs[KEY_COUNTRY] = country
            prefs[KEY_TIMEZONE] = timezone
            prefs[KEY_BIO] = bio
            prefs[KEY_MONTHLY_INCOME_TARGET] = incomeTarget
            prefs[KEY_MONTHLY_EXPENSE_LIMIT] = expenseLimit
            prefs[KEY_MONTHLY_SAVINGS_TARGET] = savingsTarget
            prefs[KEY_SAVINGS_TARGET_PERCENT] = savingsPercent
            prefs[KEY_CURRENCY_SYMBOL] = currency
            prefs[KEY_APP_LANGUAGE] = language
            prefs[KEY_THEME_MODE] = theme
            prefs[KEY_DEFAULT_ACCOUNT] = defaultAccount
            prefs[KEY_DEFAULT_PAYMENT_METHOD] = defaultPaymentMethod
            prefs[KEY_BUDGET_START_DAY] = budgetStartDay
        }
    }

    suspend fun setLanguage(language: String) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = language
        }
    }

    suspend fun setThemeMode(theme: String) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = theme
        }
    }

    suspend fun setProfilePicturePath(path: String) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_PROFILE_PICTURE_PATH] = path
        }
    }

    suspend fun updateTogglePreference(key: String, value: Boolean) {
        context.profileDataStore.edit { prefs ->
            when (key) {
                "notification" -> prefs[KEY_NOTIFICATION_ENABLED] = value
                "sound" -> prefs[KEY_SOUND_ENABLED] = value
                "haptic" -> prefs[KEY_HAPTIC_ENABLED] = value
                "pin_lock" -> prefs[KEY_PIN_LOCK_ENABLED] = value
                "biometric_lock" -> prefs[KEY_BIOMETRIC_LOCK_ENABLED] = value
                "ai_enabled" -> prefs[KEY_AI_ENABLED] = value
                "ai_fallback" -> prefs[KEY_AI_AUTO_FALLBACK] = value
            }
        }
    }

    suspend fun updateAiProvider(primary: String) {
        context.profileDataStore.edit { prefs ->
            prefs[KEY_AI_PRIMARY_PROVIDER] = primary
        }
    }
}

