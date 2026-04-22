package com.example.gametophelper.shared.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.gametophelper.shared.models.User
import com.example.gametophelper.shared.models.UserData
import com.example.gametophelper.shared.models.UserRole

class SessionManager(private val context: Context) {

    private val securePrefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "game_progress",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ========== АВТОРИЗАЦИЯ ==========
    suspend fun saveUser(login: String, password: String, token: String?, userData: UserData? = null) {
        securePrefs.edit()
            .putString("user_login", login)
            .putString("user_password", password)
            .putString("user_token", token ?: "")
            .putString("user_group", userData?.groupId ?: "")
            .putString("user_name", userData?.fullName ?: "")
            .putInt("user_course", userData?.course ?: 1)
            .apply()
        Log.d("SessionManager", "saveUser: $login")
    }

    suspend fun isLoggedIn(): Boolean {
        val loggedIn = securePrefs.getString("user_login", null) != null
        Log.d("SessionManager", "isLoggedIn: $loggedIn")
        return loggedIn
    }

    suspend fun logout() {
        securePrefs.edit().clear().apply()
        Log.d("SessionManager", "logout")
    }

    // ========== ДЛЯ ВИДЖЕТА ==========
    suspend fun hasValidSession(): Boolean {
        return isLoggedIn()
    }

    suspend fun getCurrentUser(): User? {
        val login = securePrefs.getString("user_login", null) ?: return null
        val password = securePrefs.getString("user_password", null) ?: return null
        val token = securePrefs.getString("user_token", null) ?: return null
        val groupId = securePrefs.getString("user_group", "") ?: ""
        val fullName = securePrefs.getString("user_name", null)
        val course = securePrefs.getInt("user_course", 1)

        return User(
            id = login,
            login = login,
            password = password,
            role = UserRole.STUDENT,
            groupId = groupId,
            fullName = fullName,
            course = course
        )
    }

    suspend fun getToken(): String? {
        return securePrefs.getString("user_token", null)
    }

    // ========== ПРОГРЕСС ИГРЫ ==========
    suspend fun saveMoney(money: Int) {
        securePrefs.edit().putInt("money", money).apply()
        Log.d("SessionManager", "saveMoney: $money")
    }

    suspend fun getMoney(): Int {
        val money = securePrefs.getInt("money", 0)
        Log.d("SessionManager", "getMoney: $money")
        return money
    }

    suspend fun saveExperience(exp: Int) {
        securePrefs.edit().putInt("experience", exp).apply()
        Log.d("SessionManager", "saveExperience: $exp")
    }

    suspend fun getExperience(): Int {
        val exp = securePrefs.getInt("experience", 0)
        Log.d("SessionManager", "getExperience: $exp")
        return exp
    }

    suspend fun saveSuperPowerLevel(level: Int) {
        securePrefs.edit().putInt("super_power_level", level).apply()
        Log.d("SessionManager", "saveSuperPowerLevel: $level")
    }

    suspend fun getSuperPowerLevel(): Int {
        val level = securePrefs.getInt("super_power_level", 1)
        Log.d("SessionManager", "getSuperPowerLevel: $level")
        return level
    }

    // ========== ПЕРСОНАЖ ==========
    suspend fun saveCurrentHelperIndex(index: Int) {
        securePrefs.edit().putInt("current_helper", index).apply()
        Log.d("SessionManager", "💾 saveCurrentHelperIndex: $index")
    }

    suspend fun getCurrentHelperIndex(): Int {
        val index = securePrefs.getInt("current_helper", -1)
        Log.d("SessionManager", "📖 getCurrentHelperIndex: $index")
        return index
    }

    suspend fun isHelperSelected(): Boolean = getCurrentHelperIndex() != -1
}