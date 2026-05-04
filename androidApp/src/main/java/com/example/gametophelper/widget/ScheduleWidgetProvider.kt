package com.example.gametophelper.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scheduleWidgetUpdate(context)
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
    }

    override fun onEnabled(context: Context) { scheduleWidgetUpdate(context) }
    override fun onDisabled(context: Context) { cancelWidgetUpdate(context) }

    companion object {
        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, ScheduleWidgetProvider::class.java))
            ids.forEach { updateWidget(context, appWidgetManager, it) }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val sessionManager = SessionManager(context)
                    if (!sessionManager.hasValidSession()) return@launch
                    val user = sessionManager.getCurrentUser() ?: return@launch

                    val apiClient = CollegeApiClient(context)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val lessons = apiClient.getScheduleByDate(null, dateStr)

                    withContext(Dispatchers.Main) { updateWidgetUI(context, appWidgetManager, appWidgetId, lessons) }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        private fun updateWidgetUI(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, lessons: List<Lesson>) {
            val views = RemoteViews(context.packageName, R.layout.widget_schedule)
            views.setTextViewText(R.id.widget_schedule_text, if (lessons.isEmpty()) "🎓 Нет пар" else buildScheduleText(lessons))
            views.setTextViewText(R.id.widget_update_time, "🕐 ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}")
            views.setTextViewText(R.id.widget_countdown, getCountdownToNextLesson(lessons))

            val intent = Intent(context, com.example.gametophelper.MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            views.setOnClickPendingIntent(R.id.widget_schedule_text, android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getCountdownToNextLesson(lessons: List<Lesson>): String {
            if (lessons.isEmpty()) return "✨ Отдыхай!"
            val now = Calendar.getInstance()
            val cur = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            for (l in lessons) {
                val parts = l.timeStart.split(":")
                if (parts.size >= 2) { val m = parts[0].toInt() * 60 + parts[1].toInt(); if (m > cur) { val d = m - cur; return if (d / 60 > 0) "⏳ ${d/60}ч ${d%60}м" else "⏳ ${d}м" } }
            }
            return "✨ Завтра"
        }

        private fun buildScheduleText(lessons: List<Lesson>): String {
            val sb = StringBuilder()
            lessons.forEachIndexed { i, l -> sb.append("${i+1}. ${l.timeStart} - ${l.subject}\n   👨🏫 ${l.teacher}\n   🏫 ${l.room}\n${if (i < lessons.size-1) "\n" else ""}") }
            return sb.toString()
        }

        private fun scheduleWidgetUpdate(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("widget_update", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
        }

        private fun cancelWidgetUpdate(context: Context) { WorkManager.getInstance(context).cancelUniqueWork("widget_update") }
    }
}