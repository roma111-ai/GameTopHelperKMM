package com.example.gametophelper.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.*
import com.example.gametophelper.R
import com.example.gametophelper.shared.api.CollegeApiClient
import com.example.gametophelper.shared.auth.SessionManager
import com.example.gametophelper.shared.models.Lesson
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ScheduleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scheduleWidgetUpdate(context)
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelWidgetUpdate(context)
    }

    companion object {
        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ScheduleWidgetProvider::class.java)
            )
            appWidgetIds.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    println("🔍 [Виджет] Начинаем обновление")

                    val sessionManager = SessionManager(context)

                    if (sessionManager.hasValidSession()) {
                        val token = sessionManager.getToken()

                        if (token != null) {
                            val apiClient = CollegeApiClient(context)

                            val calendar = Calendar.getInstance()
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val dateStr = dateFormat.format(calendar.time)

                            println("📅 Запрос расписания на $dateStr")

                            val lessons = apiClient.getScheduleByDate(token, dateStr)

                            println("📚 Получено ${lessons.size} пар")

                            withContext(Dispatchers.Main) {
                                updateWidgetUI(context, appWidgetManager, appWidgetId, lessons)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                updateWidgetUI(context, appWidgetManager, appWidgetId, emptyList())
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            updateWidgetUI(context, appWidgetManager, appWidgetId, emptyList())
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Ошибка виджета: ${e.message}")
                    withContext(Dispatchers.Main) {
                        updateWidgetUI(context, appWidgetManager, appWidgetId, emptyList())
                    }
                }
            }
        }

        private fun updateWidgetUI(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            lessons: List<Lesson>
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_schedule)

            val scheduleText = if (lessons.isEmpty()) {
                "🎓 Нет пар\n\nВойдите в приложение, чтобы видеть расписание"
            } else {
                buildScheduleText(lessons)
            }
            views.setTextViewText(R.id.widget_schedule_text, scheduleText)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.widget_update_time, "🕐 ${timeFormat.format(Date())}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
            println("✅ Виджет $appWidgetId обновлён")
        }

        private fun buildScheduleText(lessons: List<Lesson>): String {
            val sb = StringBuilder()
            lessons.take(5).forEachIndexed { index, lesson ->
                sb.append("${index + 1}. ${lesson.timeStart} - ${lesson.subject}\n")
                if (lesson.teacher.isNotEmpty()) {
                    sb.append("   👨‍🏫 ${lesson.teacher}\n")
                }
                if (lesson.room.isNotEmpty()) {
                    sb.append("   🏫 ${lesson.room}")
                }
                if (index < lessons.size - 1 && index < 4) {
                    sb.append("\n\n")
                }
            }
            if (lessons.size > 5) {
                sb.append("\n\n... и ещё ${lessons.size - 5} пар")
            }
            return sb.toString()
        }

        private fun scheduleWidgetUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_update",
                ExistingPeriodicWorkPolicy.KEEP,
                updateRequest
            )
        }

        private fun cancelWidgetUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("widget_update")
        }
    }
}