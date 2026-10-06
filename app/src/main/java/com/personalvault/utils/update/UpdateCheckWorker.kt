package com.personalvault.utils.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.personalvault.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Periodically checks for new releases on GitHub in the background without impacting performance or security.
 */
class UpdateCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            val result = UpdateManager.checkForUpdates(appContext)
            if (result is UpdateCheckResult.Success && result.updateInfo.isUpdateAvailable) {
                showUpdateNotification(result.updateInfo)
            }
            ListenableWorker.Result.success()
        } catch (_: Exception) {
            ListenableWorker.Result.failure()
        }
    }

    private fun showUpdateNotification(info: AppUpdateInfo) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                UpdateConfig.NOTIFICATION_CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new PersonalVault version releases"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, UpdateConfig.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("PersonalVault Update Available")
            .setContentText("Version ${info.latestVersionName} is available. Tap to view release notes.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(UpdateConfig.NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission notification omitted if user declined
        }
    }

    companion object {
        private const val WORK_NAME = "PersonalVault_Update_Check_Worker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                UpdateConfig.CHECK_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
