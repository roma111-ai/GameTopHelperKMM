package com.example.gametophelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.auth.LoginActivity
import com.example.gametophelper.shared.auth.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка...", fontSize = 24.sp)
            }
        }

        lifecycleScope.launch {
            delay(1000) // показываем загрузку 1 секунду

            val sessionManager = SessionManager(this@SplashActivity)
            val isLoggedIn = sessionManager.isLoggedIn()
            val savedHelper = sessionManager.getCurrentHelperIndex()

            val intent = when {
                !isLoggedIn -> Intent(this@SplashActivity, LoginActivity::class.java)
                savedHelper == -1 -> Intent(this@SplashActivity, ChooseHelperActivity::class.java)
                else -> Intent(this@SplashActivity, MainActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }
}