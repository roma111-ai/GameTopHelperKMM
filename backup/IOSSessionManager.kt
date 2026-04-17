package com.example.gametophelper.shared.platform

//import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.User
import com.example.gametophelper.shared.models.UserRole

actual class SessionManager {

    private var currentUser: User? = null
    private var currentToken: String? = null

    actual suspend fun saveSession(user: User, token: String) {
        currentUser = user
        currentToken = token
        println("[iOS] Session saved for ${user.login}")
    }

    actual suspend fun getCurrentUser(): User? = currentUser

    actual suspend fun getToken(): String? = currentToken

    actual suspend fun getUserRole(): UserRole? = currentUser?.role

    actual suspend fun isTeacher(): Boolean = getUserRole() == UserRole.TEACHER

    actual suspend fun isStudent(): Boolean = getUserRole() == UserRole.STUDENT

    actual suspend fun logout() {
        currentUser = null
        currentToken = null
    }
}