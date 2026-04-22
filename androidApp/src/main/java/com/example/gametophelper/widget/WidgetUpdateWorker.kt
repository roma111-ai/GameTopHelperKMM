package com.example.gametophelper.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            ScheduleWidgetProvider.updateWidget(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}