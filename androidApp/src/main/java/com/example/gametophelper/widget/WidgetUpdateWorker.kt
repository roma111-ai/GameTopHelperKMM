package com.example.gametophelper.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try { ScheduleWidgetProvider.updateWidget(applicationContext); Result.success() } catch (e: Exception) { e.printStackTrace(); Result.retry() }
    }
}