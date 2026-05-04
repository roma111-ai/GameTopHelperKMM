package com.example.gametophelper.shared.api

import android.content.Context
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.Lesson
import com.example.gametophelper.shared.models.UserData
import com.example.gametophelper.shared.models.UserType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class CollegeApiClient(private val context: Context? = null) {

    companion object {
        private const val BASE_URL = "https://msapi.top-academy.ru"
        private const val LOGIN_ENDPOINT = "/api/v2/auth/login"
        private const val SCHEDULE_ENDPOINT = "/api/v2/schedule/operations/get-by-date"
        private const val APPLICATION_KEY = "6a56a5df2667e65aab73ce76d1dd737f7d1faef9c52e8b8c55ac75f565d8e8a6"
    }

    private var currentUserType: UserType = UserType.STUDENT
    private val teacherApi = TeacherApiClient(context)

    fun setUserType(type: UserType) {
        currentUserType = type
    }

    fun getUserType(): UserType = currentUserType

    // ========== АВТОРИЗАЦИЯ (для первого входа) ==========

    suspend fun login(login: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        // 1. Пробуем студенческий API
        try {
            println("========== ПРОБУЕМ СТУДЕНТА ==========")
            println("🔑 Логин: $login")

            val url = URL("$BASE_URL$LOGIN_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("accept", "application/json, text/plain, */*")
                setRequestProperty("content-type", "application/json")
                setRequestProperty("origin", "https://journal.top-academy.ru")
                setRequestProperty("referer", "https://journal.top-academy.ru/")
                setRequestProperty("user-agent", "Mozilla/5.0")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val body = JSONObject().apply {
                put("username", login)
                put("password", password)
                put("application_key", APPLICATION_KEY)
                put("id_city", JSONObject.NULL)
            }

            connection.outputStream.write(body.toString().toByteArray())

            val responseCode = connection.responseCode
            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            connection.disconnect()

            if (responseCode in 200..299) {
                println("✅ Студент авторизован!")
                val json = JSONObject(response)
                val accessToken = json.optString("access_token", "")
                val expiresInMs = json.optLong("expires_in", 0L)
                currentUserType = UserType.STUDENT
                return@withContext LoginResult(
                    success = true,
                    token = accessToken,
                    userData = UserData("", login, 1),
                    expiresIn = expiresInMs
                )
            }
        } catch (e: Exception) {
            println("❌ Ошибка студенческой авторизации: ${e.message}")
        }

        // 2. Пробуем преподавательский API
        println("========== ПРОБУЕМ ПРЕПОДАВАТЕЛЯ ==========")
        val teacherSuccess = teacherApi.login(login, password)
        if (teacherSuccess) {
            println("✅ Преподаватель авторизован!")
            currentUserType = UserType.TEACHER
            return@withContext LoginResult(
                success = true,
                token = "teacher_session",
                expiresIn = null
            )
        }

        println("❌ Авторизация не удалась")
        return@withContext LoginResult(success = false)
    }

    // ========== ВСПОМОГАТЕЛЬНЫЙ ЛОГИН СТУДЕНТА (без смены типа) ==========

    private suspend fun loginAsStudent(login: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            println("========== ЛОГИН КАК СТУДЕНТ (без смены типа) ==========")
            println("🔑 Логин: $login")

            val url = URL("$BASE_URL$LOGIN_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                setRequestProperty("accept", "application/json, text/plain, */*")
                setRequestProperty("content-type", "application/json")
                setRequestProperty("origin", "https://journal.top-academy.ru")
                setRequestProperty("referer", "https://journal.top-academy.ru/")
                setRequestProperty("user-agent", "Mozilla/5.0")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val body = JSONObject().apply {
                put("username", login)
                put("password", password)
                put("application_key", APPLICATION_KEY)
                put("id_city", JSONObject.NULL)
            }

            connection.outputStream.write(body.toString().toByteArray())

            val responseCode = connection.responseCode
            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            connection.disconnect()

            if (responseCode in 200..299) {
                println("✅ Студент авторизован (без смены типа)!")
                val json = JSONObject(response)
                val accessToken = json.optString("access_token", "")
                val expiresInMs = json.optLong("expires_in", 0L)
                return@withContext LoginResult(
                    success = true,
                    token = accessToken,
                    userData = UserData("", login, 1),
                    expiresIn = expiresInMs
                )
            } else {
                println("❌ Ошибка логина студента: $responseCode")
                return@withContext LoginResult(success = false)
            }
        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            return@withContext LoginResult(success = false)
        }
    }

    // ========== ПОЛУЧЕНИЕ РАСПИСАНИЯ НА НЕДЕЛЮ (для приложения) ==========

    suspend fun getWeekSchedule(): Map<String, List<Lesson>> = withContext(Dispatchers.IO) {
        val sessionManager = context?.let { SessionManager(it) }
        val login = sessionManager?.getCurrentUser()?.login
        val password = sessionManager?.getCurrentUser()?.password
        val savedUserType = sessionManager?.getUserType()

        if (login == null || password == null) {
            println("❌ Нет сохранённых логина/пароля")
            return@withContext emptyMap()
        }

        println("🔍 Тип пользователя: $savedUserType")

        when (savedUserType) {
            UserType.STUDENT -> {
                // ОДИН логин для всей недели
                val loginResult = loginAsStudent(login, password)
                if (!loginResult.success || loginResult.token == null) {
                    println("❌ Не удалось авторизоваться как студент")
                    return@withContext emptyMap()
                }

                val token = loginResult.token
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val weekSchedule = mutableMapOf<String, List<Lesson>>()

                for (day in 0..6) {
                    val dateStr = dateFormat.format(calendar.time)
                    println("📅 Запрос расписания на $dateStr")
                    val lessons = getStudentSchedule(token, dateStr)
                    weekSchedule[dateStr] = lessons
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    if (day < 6) delay(300) // пауза между запросами
                }

                println("✅ Загружено дней: ${weekSchedule.size}")
                weekSchedule
            }
            UserType.TEACHER -> {
                // ОДИН логин + парсинг HTML
                val teacherSuccess = teacherApi.login(login, password)
                if (!teacherSuccess) {
                    println("❌ Не удалось авторизоваться как преподаватель")
                    return@withContext emptyMap()
                }
                val lessons = teacherApi.getSchedule(0)
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                println("✅ Преподаватель: получено ${lessons.size} уроков")
                mapOf(today to lessons)
            }
            else -> {
                println("❌ Неизвестный тип пользователя: $savedUserType")
                emptyMap()
            }
        }
    }

    // ========== ПОЛУЧЕНИЕ РАСПИСАНИЯ НА ОДИН ДЕНЬ (для виджета) ==========

    suspend fun getScheduleByDate(token: String?, date: String): List<Lesson> = withContext(Dispatchers.IO) {
        val sessionManager = context?.let { SessionManager(it) }
        val login = sessionManager?.getCurrentUser()?.login
        val password = sessionManager?.getCurrentUser()?.password
        val savedUserType = sessionManager?.getUserType()

        if (login == null || password == null) {
            println("❌ Нет сохранённых логина/пароля")
            return@withContext emptyList()
        }

        when (savedUserType) {
            UserType.STUDENT -> {
                val loginResult = loginAsStudent(login, password)
                if (loginResult.success && loginResult.token != null) {
                    getStudentSchedule(loginResult.token, date)
                } else {
                    emptyList()
                }
            }
            UserType.TEACHER -> {
                val teacherSuccess = teacherApi.login(login, password)
                if (teacherSuccess) {
                    teacherApi.getSchedule(0)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }

    // ========== ЗАПРОС РАСПИСАНИЯ СТУДЕНТА ==========

    private suspend fun getStudentSchedule(token: String?, date: String): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            println("========== ЗАПРОС РАСПИСАНИЯ СТУДЕНТА ==========")
            println("📅 Дата: $date")

            val url = URL("$BASE_URL$SCHEDULE_ENDPOINT?date_filter=$date")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("accept", "application/json")
                setRequestProperty("origin", "https://journal.top-academy.ru")
                setRequestProperty("referer", "https://journal.top-academy.ru/")
                setRequestProperty("user-agent", "Mozilla/5.0")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val responseCode = connection.responseCode
            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            connection.disconnect()

            if (responseCode in 200..299) {
                parseLessons(response)
            } else {
                println("❌ Ошибка API: $responseCode")
                emptyList()
            }

        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            emptyList()
        }
    }

    private fun parseLessons(jsonString: String): List<Lesson> {
        val lessons = mutableListOf<Lesson>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                lessons.add(
                    Lesson(
                        subject = item.optString("subject_name", ""),
                        timeStart = formatTime(item.optString("started_at", "")),
                        timeEnd = formatTime(item.optString("finished_at", "")),
                        teacher = item.optString("teacher_name", ""),
                        room = item.optString("room_name", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return lessons
    }

    private fun formatTime(timeString: String): String {
        if (timeString.isEmpty()) return ""
        return try {
            if (timeString.contains("T")) {
                val timePart = timeString.substringAfter("T")
                val hour = timePart.substringBefore(":").padStart(2, '0')
                val minute = timePart.substringAfter(":").substringBefore(":").padStart(2, '0')
                "$hour:$minute"
            } else {
                timeString.take(5)
            }
        } catch (e: Exception) {
            timeString
        }
    }
}

data class LoginResult(
    val success: Boolean,
    val token: String? = null,
    val userData: UserData? = null,
    val expiresIn: Long? = null
)