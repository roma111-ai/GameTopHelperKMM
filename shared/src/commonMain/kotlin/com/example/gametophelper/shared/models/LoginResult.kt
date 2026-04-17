package com.example.gametophelper.models

data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val error: String? = null,
    val user: UserData? = null
)

data class UserData(
    val groupId: String,
    val fullName: String,
    val course: Int,
    val studentId: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String,
    val remember: Boolean = true,
    val device: String = "android_app"
)