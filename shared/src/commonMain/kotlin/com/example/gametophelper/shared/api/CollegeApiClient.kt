package com.example.gametophelper.shared.api

import android.content.Context
import com.example.gametophelper.shared.models.Lesson
import com.example.gametophelper.shared.models.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CollegeApiClient(private val context: Context? = null) {

    companion object {
        private const val BASE_URL = "https://msapi.top-academy.ru"
        private const val LOGIN_ENDPOINT = "/api/v2/auth/login"
        private const val SCHEDULE_ENDPOINT = "/api/v2/schedule/operations/get-by-date"
        private const val APPLICATION_KEY = "6a56a5df2667e65aab73ce76d1dd737f7d1faef9c52e8b8c55ac75f565d8e8a6"
    }

    // ========== АВТОРИЗАЦИЯ ==========
    suspend fun login(login: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            println("========== АВТОРИЗАЦИЯ ==========")
            println("🔑 Логин: $login")

            val url = URL("$BASE_URL$LOGIN_ENDPOINT")
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"

                setRequestProperty("accept", "application/json, text/plain, */*")
                setRequestProperty("content-type", "application/json")
                setRequestProperty("origin", "https://journal.top-academy.ru")
                setRequestProperty("referer", "https://journal.top-academy.ru/")
                setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("accept-language", "ru_RU, ru")

                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            // Тело запроса с правильным application_key
            val body = JSONObject().apply {
                put("username", login)
                put("password", password)
                put("application_key", APPLICATION_KEY)
                put("id_city", JSONObject.NULL)
            }

            println("📦 Тело запроса: $body")
            connection.outputStream.write(body.toString().toByteArray())

            val responseCode = connection.responseCode
            println("📡 Response code: $responseCode")

            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            connection.disconnect()

            if (responseCode in 200..299) {
                println("✅ Авторизация успешна!")
                println("📦 Ответ: ${response.take(500)}")

                val json = JSONObject(response)
                val accessToken = json.optString("access_token", "")
                val refreshToken = json.optString("refresh_token", "")
                val userType = json.optInt("user_type", 1)
                val userRole = json.optString("user_role", "student")

                println("🔑 Access token: ${accessToken.take(50)}...")
                println("👤 User role: $userRole")

                val userData = UserData(
                    groupId = "",
                    fullName = login,
                    course = 1
                )

                LoginResult(
                    success = true,
                    token = accessToken,
                    userData = userData
                )
            } else {
                println("❌ Ошибка авторизации: $responseCode")
                println("Ответ: $response")
                LoginResult(success = false)
            }

        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            e.printStackTrace()
            LoginResult(success = false)
        }
    }

    // ========== ПОЛУЧЕНИЕ РАСПИСАНИЯ ==========
    suspend fun getScheduleByDate(token: String, date: String): List<Lesson> = withContext(Dispatchers.IO) {
        try {
            println("========== ЗАПРОС РАСПИСАНИЯ ==========")
            println("📅 Дата: $date")
            println("🔑 Токен: ${token.take(50)}...")

            val url = URL("$BASE_URL$SCHEDULE_ENDPOINT?date_filter=$date")
            println("🌐 URL: $url")

            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("accept", "application/json, text/plain, */*")
                setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("origin", "https://journal.top-academy.ru")
                setRequestProperty("referer", "https://journal.top-academy.ru/")
                setRequestProperty("accept-language", "ru_RU, ru")
                connectTimeout = 15000
                readTimeout = 15000
            }

            val responseCode = connection.responseCode
            println("📡 Response code: $responseCode")

            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            connection.disconnect()

            if (responseCode in 200..299) {
                println("✅ Расписание получено")
                parseLessons(response)
            } else {
                println("❌ Ошибка API: $responseCode")
                println("Ответ: ${response.take(500)}")
                emptyList()
            }

        } catch (e: Exception) {
            println("❌ Ошибка: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // ========== ПАРСИНГ ==========
    private fun parseLessons(jsonString: String): List<Lesson> {
        val lessons = mutableListOf<Lesson>()

        try {
            val jsonArray = JSONArray(jsonString)
            println("📊 Найдено элементов: ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val lesson = Lesson(
                    subject = item.optString("subject_name", "Без названия"),
                    timeStart = formatTime(item.optString("started_at", "")),
                    teacher = item.optString("teacher_name", "Не указан"),
                    room = item.optString("room_name", "Не указана")
                )
                lessons.add(lesson)
                println("📚 ${lesson.timeStart} - ${lesson.subject}")
            }

        } catch (e: Exception) {
            println("❌ Ошибка парсинга: ${e.message}")
        }

        return lessons
    }

    private fun formatTime(timeString: String): String {
        return try {
            if (timeString.contains("T")) {
                val timePart = timeString.substringAfter("T")
                timePart.substringBefore(":").take(2) + ":" +
                        timePart.substringAfter(":").substringBefore(":").take(2)
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
    val userData: UserData? = null
)