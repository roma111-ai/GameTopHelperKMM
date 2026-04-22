package com.example.gametophelper.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val groupId: String,
    val fullName: String,
    val course: Int,
    val studentId: String? = null
)