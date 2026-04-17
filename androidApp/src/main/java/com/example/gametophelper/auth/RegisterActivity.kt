import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val login = binding.etLogin.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val groupId = binding.etGroup.text.toString()

            if (validateInputs(login, password, confirmPassword, groupId)) {
                performRegistration(login, password, groupId)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(
        login: String,
        password: String,
        confirmPassword: String,
        groupId: String
    ): Boolean {
        if (login.isEmpty()) {
            Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return false
        }

        if (groupId.isEmpty()) {
            Toast.makeText(this, "Введите номер группы", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun performRegistration(login: String, password: String, groupId: String) {
        lifecycleScope.launch {
            try {
                // TODO: Здесь будет реальная регистрация через API колледжа
                // Пока просто проверяем, что пароль не пустой

                // Имитация задержки
                Thread.sleep(500)

                // Создаём нового пользователя
                val user = com.example.gametophelper.models.User(
                    login = login,
                    password = password,
                    groupId = groupId,
                    fullName = "Новый пользователь",
                    course = 1
                )

                // Сохраняем сессию и автоматически входим
                sessionManager.saveSession(user, "temp_token", rememberMe = true)

                Toast.makeText(this@RegisterActivity,
                    "Регистрация успешна!", Toast.LENGTH_LONG).show()

                // Переходим в игру
                val intent = Intent(this@RegisterActivity,
                    com.example.gametophelper.MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity,
                    "Ошибка регистрации: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}