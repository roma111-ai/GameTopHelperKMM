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
import com.example.gametophelper.ui.theme.GameTopHelperKMMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: CollegeApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        apiClient = CollegeApiClient(this)

        lifecycleScope.launch {
            val hasSession = sessionManager.hasValidSession()
            val savedHelper = sessionManager.getCurrentHelperIndex()

            if (hasSession && savedHelper != -1) {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else if (hasSession && savedHelper == -1) {
                startActivity(Intent(this@LoginActivity, ChooseHelperActivity::class.java))
                finish()
            } else {
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
                setContent {
                    GameTopHelperKMMTheme {
                        LoadingScreen(message = "Авторизация...")
                    }
                }

                delay(500)

                try {
                    val result = apiClient.login(login, password)

                    if (result.success) {
                        val userType = apiClient.getUserType()
                        sessionManager.saveUser(
                            login = login,
                            password = password,
                            token = result.token,
                            userType = userType,
                            userData = result.userData,
                            expiresIn = result.expiresIn?.let { it / 1000 }
                        )
                        sessionManager.saveUserType(userType)

                        Toast.makeText(
                            this@LoginActivity,
                            "Добро пожаловать!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@LoginActivity, ChooseHelperActivity::class.java))
                        finish()
                    } else {
                        setContent {
                            GameTopHelperKMMTheme {
                                LoginScreen(
                                    onLoginClick = { l, p ->
                                        performLogin(l, p)
                                    }
                                )
                            }
                        }
                        Toast.makeText(
                            this@LoginActivity,
                            "Неверный логин или пароль",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    setContent {
                        GameTopHelperKMMTheme {
                            LoginScreen(
                                onLoginClick = { l, p ->
                                    performLogin(l, p)
                                }
                            )
                        }
                    }
                    Toast.makeText(
                        this@LoginActivity,
                        "Ошибка: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Введите логин и пароль",
                    Toast.LENGTH_SHORT
                ).show()
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

@Composable
fun LoadingScreen(message: String = "Загрузка...") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}