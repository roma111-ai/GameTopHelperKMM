package com.example.gametophelper

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gametophelper.shared.api.CollegeApiClient
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.Lesson
import com.example.gametophelper.shared.models.UserType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduleFullScreen(sessionManager: SessionManager) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val apiClient = remember { CollegeApiClient(context) }

    var isLoading by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(0) }
    var weeklySchedule by remember { mutableStateOf<Map<String, List<Lesson>>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userType by remember { mutableStateOf(UserType.STUDENT) }

    val daysOfWeek = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    val calendar = Calendar.getInstance()
    val currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                userType = sessionManager.getUserType()
                apiClient.setUserType(userType)

                // Используем новый единый метод
                val schedule = apiClient.getWeekSchedule()

                if (schedule.isEmpty()) {
                    errorMessage = "Не удалось загрузить расписание"
                } else {
                    // Преобразуем Map<Date, List<Lesson>> в Map<DayName, List<Lesson>>
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val daySchedule = mutableMapOf<String, List<Lesson>>()

                    // Сортируем даты
                    val sortedDates = schedule.keys.sorted()

                    sortedDates.forEachIndexed { index, dateStr ->
                        if (index < 7) {
                            daySchedule[daysOfWeek[index]] = schedule[dateStr] ?: emptyList()
                            println("📅 ${daysOfWeek[index]} ($dateStr): получено ${schedule[dateStr]?.size ?: 0} уроков")
                        }
                    }

                    // Если дней меньше 7 (например, у препода), заполняем остальные
                    for (i in sortedDates.size until 7) {
                        daySchedule[daysOfWeek[i]] = emptyList()
                    }

                    weeklySchedule = daySchedule
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
                println("❌ Ошибка загрузки расписания: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a2e)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок с типом пользователя
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 РАСПИСАНИЕ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (userType == UserType.TEACHER) Color(0xFF4CAF50) else Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (userType == UserType.TEACHER) "👨‍🏫 Преподаватель" else "🎓 Студент",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Дни недели
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    Button(
                        onClick = { selectedDay = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDay == index) Color(0xFF4CAF50) else Color(0xFF2a2a3e)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            color = if (selectedDay == index || index == currentDayOfWeek) Color.White else Color.Gray,
                            fontWeight = if (selectedDay == index || index == currentDayOfWeek) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage!!, color = Color.Red)
                    }
                }
                weeklySchedule != null -> {
                    val lessons = weeklySchedule!![daysOfWeek[selectedDay]] ?: emptyList()

                    if (lessons.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎓", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Нет пар на ${daysOfWeek[selectedDay]}",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lessons) { lesson ->
                                LessonFullCard(lesson = lesson)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonFullCard(lesson: Lesson) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2a2a3e)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = if (lesson.timeEnd.isNotEmpty()) {
                    "${lesson.timeStart} - ${lesson.timeEnd}"
                } else {
                    lesson.timeStart
                },
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lesson.subject,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (lesson.teacher.isNotEmpty() && lesson.teacher != "Не указан") {
                Text(
                    text = "👨‍🏫 ${lesson.teacher}",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            }
            if (lesson.room.isNotEmpty() && lesson.room != "Не указана") {
                Text(
                    text = "🏫 ${lesson.room}",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            }
        }
    }
}