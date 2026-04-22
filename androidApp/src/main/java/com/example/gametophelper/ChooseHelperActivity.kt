package com.example.gametophelper

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.gametophelper.shared.GameBridge
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.ui.theme.GameTopHelperKMMTheme
import kotlinx.coroutines.launch

class ChooseHelperActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        GameBridge.initGame(false)

        setContent {
            GameTopHelperKMMTheme {
                ChooseHelperScreen(
                    onHelperSelected = { index, name ->
                        println("🔹 Выбран помощник: index=$index, name=$name")

                        GameBridge.selectHelper(index)

                        lifecycleScope.launch {
                            sessionManager.saveCurrentHelperIndex(index)
                            val saved = sessionManager.getCurrentHelperIndex()
                            println("💾 После сохранения: saved=$saved")
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ChooseHelperScreen(onHelperSelected: (Int, String) -> Unit) {
    val helpers = listOf(
        Triple(0, "Кошка", R.drawable.catmain),
        Triple(1, "Лис", R.drawable.foxmain),
        Triple(2, "Собака", R.drawable.dogmain),
        Triple(3, "Попугай", R.drawable.parrotmain)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Выбери своего помощника!",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(helpers.size) { index ->
                val (helperIndex, helperName, imageRes) = helpers[index]
                HelperCard(
                    name = helperName,
                    imageRes = imageRes,
                    onClick = { onHelperSelected(helperIndex, helperName) }
                )
            }
        }
    }
}

@Composable
fun HelperCard(name: String, imageRes: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = name,
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = name,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}