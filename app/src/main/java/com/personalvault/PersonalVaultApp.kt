package com.personalvault

import android.app.Application
import com.personalvault.utils.update.UpdateCheckWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PersonalVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule non-intrusive periodic update checks via WorkManager
        try {
            UpdateCheckWorker.schedule(this)
        } catch (_: Exception) {
            // WorkManager initialization fallback
        }
    }
}
