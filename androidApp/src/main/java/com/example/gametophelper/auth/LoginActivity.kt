import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.MainActivity
import com.example.gametophelper.R
import com.example.gametophelper.api.CollegeApiClient
import com.example.gametophelper.databinding.ActivityLoginBinding
import com.example.gametophelper.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiClient: CollegeApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiClient = CollegeApiClient(this)

        setupClickListeners()
        checkSavedCredentials()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val login = binding.etLogin.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInputs(login, password)) {
                performLogin(login, password)
            }
        }

        // ИСПРАВЛЕНО: Убираем кнопку регистрации или делаем ее неактивной
        binding.btnRegister.visibility = View.GONE
        // Или можно оставить, но с другим текстом:
        // binding.btnRegister.text = "Нет аккаунта?"
        // binding.btnRegister.setOnClickListener {
        //     Toast.makeText(this, "Обратитесь в деканат для получения логина", Toast.LENGTH_LONG).show()
        // }
    }

    private fun checkSavedCredentials() {
        lifecycleScope.launch {
            if (sessionManager.hasValidSession()) {
                val user = sessionManager.getCurrentUser()
                if (user != null) {
                    showLoading(true)

                    val apiResult = apiClient.login(user.login, user.password)

                    showLoading(false)

                    if (apiResult.success) {
                        navigateToMainActivity()
                    } else {
                        sessionManager.logout()
                        Toast.makeText(this@LoginActivity,
                            "Сессия истекла, войдите заново",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun validateInputs(login: String, password: String): Boolean {
        return when {
            login.isEmpty() -> {
                Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnLogin.text = if (isLoading) "Вход..." else "Войти"
    }

    private fun performLogin(login: String, password: String) {
        lifecycleScope.launch {
            try {
                showLoading(true)

                val apiResult = apiClient.login(login, password)

                showLoading(false)

                if (apiResult.success && apiResult.user != null) {
                    val user = User(
                        login = login,
                        password = password,
                        groupId = apiResult.user.groupId,
                        fullName = apiResult.user.fullName,
                        course = apiResult.user.course
                    )

                    sessionManager.saveSession(user, apiResult.token!!, binding.cbRememberMe.isChecked)

                    Toast.makeText(this@LoginActivity,
                        "Добро пожаловать, ${user.fullName}!",
                        Toast.LENGTH_LONG).show()

                    navigateToMainActivity()
                } else {
                    Toast.makeText(this@LoginActivity,
                        apiResult.error ?: "Ошибка входа",
                        Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@LoginActivity,
                    "Ошибка: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}