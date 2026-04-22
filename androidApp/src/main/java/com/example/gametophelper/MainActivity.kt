package com.example.gametophelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.shared.GameBridge
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.ui.theme.GameTopHelperKMMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        lifecycleScope.launch {
            loadSavedProgress()
        }

        setContent {
            GameTopHelperKMMTheme {
                MainScreen(
                    onSaveProgress = { money, exp, powerLevel, helperIndex ->
                        lifecycleScope.launch {
                            sessionManager.saveMoney(money)
                            sessionManager.saveExperience(exp)
                            sessionManager.saveSuperPowerLevel(powerLevel)
                            sessionManager.saveCurrentHelperIndex(helperIndex)
                        }
                    }
                )
            }
        }
    }

    private suspend fun loadSavedProgress() {
        val savedMoney = sessionManager.getMoney()
        val savedExp = sessionManager.getExperience()
        val savedPowerLevel = sessionManager.getSuperPowerLevel()
        val savedHelper = sessionManager.getCurrentHelperIndex()

        println("========================================")
        println("📦 Загрузка сохранённых данных:")
        println("💰 Деньги: $savedMoney")
        println("🎓 Опыт: $savedExp")
        println("⚡ Уровень суперсилы: $savedPowerLevel")
        println("🐱 Помощник: $savedHelper")
        println("========================================")

        // Загружаем всё сразу в C++
        GameBridge.loadGameData(savedMoney, savedExp, savedPowerLevel, savedHelper)

        // Инициализируем игру
        GameBridge.initGame(false)
    }
}

@Composable
fun MainScreen(
    onSaveProgress: (money: Int, exp: Int, powerLevel: Int, helperIndex: Int) -> Unit
) {
    var money by remember { mutableStateOf(GameBridge.getMoney()) }
    var experience by remember { mutableStateOf(GameBridge.getExperience()) }
    var superPowerLevel by remember { mutableStateOf(GameBridge.getSuperPowerLevel()) }
    var helperIndex by remember { mutableStateOf(GameBridge.getCurrentHelperIndex()) }
    var helperName by remember { mutableStateOf(GameBridge.getCurrentHelperName()) }

    var selectedTab by remember { mutableStateOf(0) }
    var tapAnimation by remember { mutableStateOf(false) }
    val maxExperience = 100

    val helperImage = when (helperIndex) {
        0 -> R.drawable.catmain
        1 -> R.drawable.foxmain
        2 -> R.drawable.dogmain
        3 -> R.drawable.parrotmain
        else -> R.drawable.catmain
    }

    fun updateUI() {
        money = GameBridge.getMoney()
        experience = GameBridge.getExperience()
        superPowerLevel = GameBridge.getSuperPowerLevel()
        helperIndex = GameBridge.getCurrentHelperIndex()
        helperName = GameBridge.getCurrentHelperName()
        onSaveProgress(money, experience, superPowerLevel, helperIndex)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🏠", fontSize = 20.sp) },
                    label = { Text("Главная", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🛒", fontSize = 20.sp) },
                    label = { Text("Магазин", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("🎮", fontSize = 20.sp) },
                    label = { Text("Игры", fontSize = 12.sp) }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    helperImage = helperImage,
                    helperName = helperName,
                    money = money,
                    experience = experience,
                    maxExperience = maxExperience,
                    superPowerLevel = superPowerLevel,
                    onTap = {
                        GameBridge.addMoney(1)
                        GameBridge.addExperience(1)
                        tapAnimation = true
                        updateUI()
                    },
                    onSuperPower = {
                        GameBridge.addMoney(50 * superPowerLevel)
                        updateUI()
                    },
                    onUpgradeSuperPower = {
                        if (GameBridge.upgradeSuperPower()) {
                            updateUI()
                        }
                    },
                    tapAnimation = tapAnimation,
                    onAnimationComplete = { tapAnimation = false }
                )
                1 -> ShopScreen()
                2 -> GamesScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(
    helperImage: Int,
    helperName: String,
    money: Int,
    experience: Int,
    maxExperience: Int,
    superPowerLevel: Int,
    onTap: () -> Unit,
    onSuperPower: () -> Unit,
    onUpgradeSuperPower: () -> Unit,
    tapAnimation: Boolean,
    onAnimationComplete: () -> Unit
) {
    if (tapAnimation) {
        LaunchedEffect(Unit) {
            delay(200)
            onAnimationComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
    ) {
        Image(
            painter = painterResource(R.drawable.bacgraound_test),
            contentDescription = "Фон",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💰", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$money",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎓 $experience / $maxExperience",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        LinearProgressIndicator(
                            progress = experience.toFloat() / maxExperience,
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp),
                            color = Color(0xFFFFEB3B),
                            trackColor = Color.Gray
                        )
                    }
                }
            }

            Text(
                text = helperName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.weight(0.2f))

            Box(
                modifier = Modifier
                    .size(360.dp)
                    .clip(CircleShape)
                    .clickable { onTap() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(helperImage),
                    contentDescription = helperName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
                if (tapAnimation) {
                    Text(
                        text = "+1",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onSuperPower,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = CircleShape,
                        modifier = Modifier.size(70.dp)
                    ) {
                        Text("💥", fontSize = 32.sp)
                    }
                    Text(
                        text = "Суперсила",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "ур. $superPowerLevel",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onUpgradeSuperPower,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = CircleShape,
                        modifier = Modifier.size(60.dp)
                    ) {
                        Text("⬆️", fontSize = 24.sp)
                    }
                    Text(
                        text = "Прокачка",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Text(
                        text = "500💰",
                        fontSize = 10.sp,
                        color = Color(0xFFFFD700)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShopScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🛒 Магазин скоро появится!",
            fontSize = 24.sp,
            color = Color.White
        )
    }
}

@Composable
fun GamesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🎮 Мини-игры скоро появятся!",
            fontSize = 24.sp,
            color = Color.White
        )
    }
}