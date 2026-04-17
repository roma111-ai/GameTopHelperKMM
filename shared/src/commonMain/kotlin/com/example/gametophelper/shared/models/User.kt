package com.example.gametophelper.models

import java.util.*

data class User(
    val id: String = UUID.randomUUID().toString(),
    val login: String,
    val password: String,
    val groupId: String,
    val fullName: String? = null,  // ← Nullable
    val course: Int? = null,       // ← Nullable
    val lastLoginTime: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)