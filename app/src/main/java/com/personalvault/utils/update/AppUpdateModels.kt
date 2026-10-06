package com.personalvault.utils.update

import java.io.File

/**
 * Encapsulates release and update metadata from GitHub Releases API.
 */
data class AppUpdateInfo(
    val currentVersionName: String,
    val currentVersionCode: Long,
    val latestVersionName: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val releaseDate: String,
    val isUpdateAvailable: Boolean,
    val apkSize: Long = 0L,
    val tagName: String = ""
)

/**
 * Result state when checking for updates against GitHub Releases API.
 */
sealed class UpdateCheckResult {
    data class Success(val updateInfo: AppUpdateInfo) : UpdateCheckResult()
    data class NoUpdate(val updateInfo: AppUpdateInfo) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

/**
 * Progress and status state during APK file download.
 */
sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(
        val progressPct: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long
    ) : DownloadStatus()
    data class Success(val apkFile: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}
