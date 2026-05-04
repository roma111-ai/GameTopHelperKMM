package com.example.gametophelper.shared.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.gametophelper.shared.models.User
import com.example.gametophelper.shared.models.UserData
import com.example.gametophelper.shared.models.UserRole
import com.example.gametophelper.shared.models.UserType

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

    suspend fun saveUser(
        login: String,
        password: String,
        token: String?,
        userType: UserType?,
        userData: UserData? = null,
        expiresIn: Long? = null
    ) {
        securePrefs.edit()
            .putString("user_login", login)
            .putString("user_password", password)
            .putString("user_token", token ?: "")
            .putString("user_type", userType?.name)
            .putString("user_group", userData?.groupId ?: "")
            .putString("user_name", userData?.fullName ?: "")
            .putInt("user_course", userData?.course ?: 1)
            .apply()
        if (expiresIn != null) {
            securePrefs.edit().putLong("expires_at", System.currentTimeMillis() + (expiresIn * 1000)).apply()
        }
    }

    suspend fun isLoggedIn(): Boolean = securePrefs.getString("user_login", null) != null
    suspend fun logout() { securePrefs.edit().clear().apply() }
    suspend fun hasValidSession(): Boolean = isLoggedIn()

    suspend fun getCurrentUser(): User? {
        val login = securePrefs.getString("user_login", null) ?: return null
        val password = securePrefs.getString("user_password", null) ?: return null
        val groupId = securePrefs.getString("user_group", "") ?: ""
        val fullName = securePrefs.getString("user_name", null)
        val course = securePrefs.getInt("user_course", 1)
        return User(id = login, login = login, password = password, role = UserRole.STUDENT, groupId = groupId, fullName = fullName, course = course)
    }

    suspend fun getToken(): String? = securePrefs.getString("user_token", null)
    suspend fun saveMoney(money: Int) { securePrefs.edit().putInt("money", money).apply() }
    suspend fun getMoney(): Int = securePrefs.getInt("money", 0)
    suspend fun saveExperience(exp: Int) { securePrefs.edit().putInt("experience", exp).apply() }
    suspend fun getExperience(): Int = securePrefs.getInt("experience", 0)
    suspend fun saveSuperPowerLevel(level: Int) { securePrefs.edit().putInt("super_power_level", level).apply() }
    suspend fun getSuperPowerLevel(): Int = securePrefs.getInt("super_power_level", 1)
    suspend fun saveCurrentHelperIndex(index: Int) { securePrefs.edit().putInt("current_helper", index).apply() }
    suspend fun getCurrentHelperIndex(): Int = securePrefs.getInt("current_helper", -1)
    suspend fun isHelperSelected(): Boolean = getCurrentHelperIndex() != -1

    suspend fun saveUserType(userType: UserType) { securePrefs.edit().putString("user_type", userType.name).apply() }
    suspend fun getUserType(): UserType {
        val type = securePrefs.getString("user_type", UserType.STUDENT.name)
        return try { UserType.valueOf(type ?: UserType.STUDENT.name) } catch (e: Exception) { UserType.STUDENT }
    }

    suspend fun saveTeacherCookies(cookies: String) {
        securePrefs.edit().putString("teacher_cookies", cookies).apply()
        securePrefs.edit().putLong("teacher_cookies_expires_at", System.currentTimeMillis() + 24 * 60 * 60 * 1000).apply()
    }
    suspend fun getTeacherCookies(): String? {
        val cookies = securePrefs.getString("teacher_cookies", null) ?: return null
        return if (System.currentTimeMillis() < securePrefs.getLong("teacher_cookies_expires_at", 0L)) cookies else null
    }

    suspend fun isGiftReceived(): Boolean = securePrefs.getBoolean("gift_received", false)
    suspend fun setGiftReceived() { securePrefs.edit().putBoolean("gift_received", true).apply() }
    suspend fun getExclusiveHelperIndex(): Int = securePrefs.getInt("exclusive_helper_index", -1)
    suspend fun saveExclusiveHelperIndex(index: Int) { securePrefs.edit().putInt("exclusive_helper_index", index).apply() }
}