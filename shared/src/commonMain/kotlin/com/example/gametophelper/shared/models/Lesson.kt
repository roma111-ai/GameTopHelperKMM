package com.example.gametophelper.models

data class Lesson(
    val id: String,
    val subject: String,      // Название предмета
    val timeStart: String,    // "09:00"
    val timeEnd: String,      // "10:30"
    val teacher: String,      // Преподаватель
    val room: String,         // Аудитория
    val type: String,         // Лекция/Практика
    val group: String,        // Группа
    val weekDay: Int,         // 1-6 (пн-сб)
    val weekType: String? = null, // Чет/нечет
    val isOnline: Boolean = false
)

data class Schedule(
    val date: String,
    val lessons: List<Lesson>,
    val lastUpdate: Long = System.currentTimeMillis()
)