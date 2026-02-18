package com.clearsky.weather.ui.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker that periodically fetches weather data
 * and updates all ClearSky widgets.
 */
class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "WidgetUpdateWorker"
        private const val WORK_NAME = "clearsky_widget_update"
        private const val UPDATE_INTERVAL_MINUTES = 30L

        /**
         * Enqueue periodic widget updates.
         * Called when the first widget is added or on app start.
         */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                UPDATE_INTERVAL_MINUTES, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        /**
         * Cancel periodic widget updates.
         * Called when the last widget is removed.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Force an immediate one-time update of all widgets.
         */
        suspend fun updateAllWidgets(context: Context) {
            try {
                SmallWeatherWidget().updateAll(context)
                MediumWeatherWidget().updateAll(context)
                LargeWeatherWidget().updateAll(context)
                XLWeatherWidget().updateAll(context)
                AqiWidget().updateAll(context)
                CompactForecastWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting widget update work")

            // Pre-fetch data so it's cached for all widgets
            WidgetDataProvider.getWidgetData(context)

            // Update all widget instances
            updateAllWidgets(context)

            Log.d(TAG, "Widget update work completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Widget update work failed", e)
            Result.retry()
        }
    }
}
