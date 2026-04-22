package com.example.gametophelper.shared.models

data class Lesson(
    val id: String = "",
    val subject: String = "",
    val timeStart: String = "",
    val timeEnd: String = "",
    val teacher: String = "",
    val room: String = "",
    val type: String = "",
    val group: String = "",
    val weekDay: Int = 0,
    val weekType: String? = null,
    val isOnline: Boolean = false
)