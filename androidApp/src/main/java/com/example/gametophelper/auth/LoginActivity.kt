package com.example.gametophelper.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.ChooseHelperActivity
import com.example.gametophelper.MainActivity
import com.example.gametophelper.shared.api.CollegeApiClient
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.UserData
import com.example.gametophelper.ui.theme.GameTopHelperKMMTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: CollegeApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        apiClient = CollegeApiClient(this)

        lifecycleScope.launch {
            // Проверяем, есть ли сохранённая сессия
            val hasSession = sessionManager.hasValidSession()

            if (hasSession) {
                // Проверяем, выбран ли персонаж
                val savedHelper = sessionManager.getCurrentHelperIndex()

                if (savedHelper == -1) {
                    // Есть сессия, но персонаж не выбран
                    startActivity(Intent(this@LoginActivity, ChooseHelperActivity::class.java))
                } else {
                    // Есть сессия и персонаж выбран
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                }
                finish()
            } else {
                // Нет сессии - показываем экран входа
                setContent {
                    GameTopHelperKMMTheme {
                        LoginScreen(
                            onLoginClick = { login, password ->
                                performLogin(login, password)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun performLogin(login: String, password: String) {
        lifecycleScope.launch {
            if (login.isNotBlank() && password.isNotBlank()) {
                try {
                    Toast.makeText(this@LoginActivity, "Авторизация...", Toast.LENGTH_SHORT).show()

                    val result = apiClient.login(login, password)

                    // Проверяем, что токен не null
                    if (result.success && result.token != null) {
                        val realToken = result.token  // теперь это String, а не String?

                        // Конвертируем UserData
                        val userData = result.userData?.let {
                            UserData(
                                groupId = it.groupId,
                                fullName = it.fullName,
                                course = it.course
                            )
                        }

                        // Сохраняем с реальным токеном (non-null)
                        sessionManager.saveUser(login, password, realToken, userData)

                        Toast.makeText(this@LoginActivity, "Добро пожаловать!", Toast.LENGTH_LONG).show()

                        startActivity(Intent(this@LoginActivity, ChooseHelperActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginClick: (String, String) -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GameTopHelper",
            fontSize = 32.sp,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLoginClick(login, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти")
        }
    }
}