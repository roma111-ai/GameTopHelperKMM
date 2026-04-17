package com.example.gametophelper.shared.api

import com.example.gametophelper.shared.models.LoginRequest
import com.example.gametophelper.shared.models.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CollegeApiClient {

    private val BASE_URL = "https://msapi.top-academy.ru"
    private val LOGIN_URL = "$BASE_URL/api/v2/auth/login"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun login(username: String, password: String): LoginResponse {
        return try {
            val request = LoginRequest(
                username = username,
                password = password,
                remember = true,
                device = "android_app"
            )

            val response: LoginResponse = client.post(LOGIN_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            response

        } catch (e: Exception) {
            LoginResponse(
                success = false,
                error = "Ошибка подключения: ${e.message}"
            )
        }
    }
}