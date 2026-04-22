package com.example.gametophelper.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val login: String,
    val password: String,
    val token: String = "",
    val role: UserRole,
    val groupId: String,
    val fullName: String? = null,
    val course: Int? = null,
    val lastLoginTime: Long = 0
)