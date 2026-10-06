package com.personalvault.utils.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.personalvault.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Handles checking GitHub Releases for updates, downloading APK files securely over HTTPS,
 * and triggering standard Android Package Installation flow.
 */
object UpdateManager {

    /**
     * Checks the official GitHub repository for the latest release.
     */
    suspend fun checkForUpdates(context: Context): UpdateCheckResult = withContext(Dispatchers.IO) {
        try {
            val (currentVersionName, currentVersionCode) = getCurrentAppVersion(context)

            val url = URL(UpdateConfig.RELEASES_API_URL)
            val connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
                setRequestProperty("User-Agent", "PersonalVault-AndroidApp")
                setRequestProperty("Accept", "application/vnd.github.v3+json")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext UpdateCheckResult.Error("No GitHub releases found for ${UpdateConfig.GITHUB_OWNER}/${UpdateConfig.GITHUB_REPO}.")
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext UpdateCheckResult.Error("GitHub API returned HTTP $responseCode.")
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val tagName = json.optString("tag_name", "")
            val rawVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val releaseNotes = json.optString("body", "No release notes provided.")
            val publishedAt = json.optString("published_at", "")
            val releaseDate = if (publishedAt.length >= 10) publishedAt.substring(0, 10) else publishedAt

            var downloadUrl: String? = null
            var apkSize = 0L

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val assetName = asset.optString("name", "")
                    val contentType = asset.optString("content_type", "")
                    if (assetName.endsWith(".apk", ignoreCase = true) ||
                        contentType == "application/vnd.android.package-archive"
                    ) {
                        downloadUrl = asset.optString("browser_download_url")
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (downloadUrl.isNullOrEmpty()) {
                downloadUrl = json.optString("html_url", "")
            }

            val latestVersionName = if (rawVersion.isNotEmpty()) rawVersion else "1.0.0"
            val updateAvailable = isNewerVersion(currentVersionName, latestVersionName)

            val updateInfo = AppUpdateInfo(
                currentVersionName = currentVersionName,
                currentVersionCode = currentVersionCode,
                latestVersionName = latestVersionName,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                releaseDate = releaseDate,
                isUpdateAvailable = updateAvailable,
                apkSize = apkSize,
                tagName = tagName
            )

            if (updateAvailable) {
                UpdateCheckResult.Success(updateInfo)
            } else {
                UpdateCheckResult.NoUpdate(updateInfo)
            }
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.message ?: "Failed to check for updates.")
        }
    }

    /**
     * Downloads the release APK over HTTPS with progress updates.
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        onProgress: (DownloadStatus) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(DownloadStatus.Downloading(0, 0, 0))

            var currentUrl = downloadUrl
            var connection: HttpsURLConnection? = null
            var redirects = 0

            // Handle HTTP 302 / 301 redirects commonly used by GitHub Release asset downloads
            while (redirects < 5) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpsURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", "PersonalVault-AndroidApp")
                    instanceFollowRedirects = true
                }

                val code = connection.responseCode
                if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrEmpty()) {
                        currentUrl = location
                        redirects++
                        continue
                    }
                }
                break
            }

            if (connection == null || connection.responseCode != HttpURLConnection.HTTP_OK) {
                val code = connection?.responseCode ?: -1
                onProgress(DownloadStatus.Error("Failed to download APK file (HTTP $code)."))
                return@withContext
            }

            val totalBytes = connection.contentLengthLong
            val downloadDir = File(context.getExternalFilesDir(null), "Download").apply { mkdirs() }
            val outputFile = File(downloadDir, "PersonalVault-update.apk")

            connection.inputStream.use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesDownloaded = 0L
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesDownloaded += read

                        val progressPct = if (totalBytes > 0) {
                            ((bytesDownloaded * 100) / totalBytes).toInt()
                        } else {
                            -1
                        }

                        onProgress(DownloadStatus.Downloading(progressPct, bytesDownloaded, totalBytes))
                    }
                    outputStream.flush()
                }
            }

            // Verify downloaded APK validity
            val pm = context.packageManager
            val packageInfo = pm.getPackageArchiveInfo(outputFile.absolutePath, 0)
            if (packageInfo == null) {
                outputFile.delete()
                onProgress(DownloadStatus.Error("Downloaded APK file is invalid or corrupted."))
            } else {
                onProgress(DownloadStatus.Success(outputFile))
            }

        } catch (e: Exception) {
            onProgress(DownloadStatus.Error(e.message ?: "An error occurred during download."))
        }
    }

    /**
     * Returns true if the app currently has permission to install packages from unknown sources.
     */
    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens system settings screen for enabling unknown app installations for this package.
     */
    fun openInstallUnknownAppsSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Launches Android's standard package installer for the given downloaded APK file.
     * Uses FileProvider and respects user consent without root or silent installation.
     */
    fun installApk(context: Context, apkFile: File): Boolean {
        if (!canRequestPackageInstalls(context)) {
            openInstallUnknownAppsSettings(context)
            return false
        }

        return try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads current package version name and version code.
     */
    fun getCurrentAppVersion(context: Context): Pair<String, Long> {
        return try {
            val pm = context.packageManager
            val packageInfo = pm.getPackageInfo(context.packageName, 0)
            val name = packageInfo.versionName ?: BuildConfig.VERSION_NAME
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            Pair(name, code)
        } catch (e: Exception) {
            Pair(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toLong())
        }
    }

    /**
     * Compares two semantic version strings (e.g., "1.0.0" vs "1.0.1").
     * Returns true if [latestVersion] is strictly newer than [currentVersion].
     */
    fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        try {
            val currentParts = currentVersion.split(".", "-").mapNotNull { it.toIntOrNull() }
            val latestParts = latestVersion.split(".", "-").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currentParts.size, latestParts.size)
            for (i in 0 until maxLen) {
                val curr = currentParts.getOrElse(i) { 0 }
                val lat = latestParts.getOrElse(i) { 0 }
                if (lat > curr) return true
                if (lat < curr) return false
            }
        } catch (_: Exception) {
            // Fallback string comparison
            return latestVersion.compareTo(currentVersion, ignoreCase = true) > 0
        }
        return false
    }
}
