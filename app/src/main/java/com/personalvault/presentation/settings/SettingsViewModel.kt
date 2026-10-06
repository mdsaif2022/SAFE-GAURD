package com.personalvault.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.UserSession
import com.personalvault.domain.repository.AuthRepository
import com.personalvault.utils.update.AppUpdateInfo
import com.personalvault.utils.update.DownloadStatus
import com.personalvault.utils.update.UpdateCheckResult
import com.personalvault.utils.update.UpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val session: UserSession? = null,
    val isLoggingOut: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val updateInfo: AppUpdateInfo? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.Idle,
    val showUpdateDialog: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        authRepository.getCurrentSession()
            .onEach { session ->
                _uiState.value = _uiState.value.copy(session = session)
            }
            .launchIn(viewModelScope)
    }

    fun switchRole(newMode: DeviceMode, onSwitched: () -> Unit) {
        authRepository.updateDeviceMode(newMode)
            .onEach {
                onSwitched()
            }
            .launchIn(viewModelScope)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            authRepository.logout()
            _uiState.value = _uiState.value.copy(isLoggingOut = false)
            onLoggedOut()
        }
    }

    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingUpdate = true,
                errorMessage = null,
                infoMessage = null
            )

            when (val result = UpdateManager.checkForUpdates(context)) {
                is UpdateCheckResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        updateInfo = result.updateInfo,
                        showUpdateDialog = true,
                        downloadStatus = DownloadStatus.Idle
                    )
                }
                is UpdateCheckResult.NoUpdate -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        updateInfo = result.updateInfo,
                        infoMessage = "You're using the latest version."
                    )
                }
                is UpdateCheckResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun startApkDownload(context: Context) {
        val url = _uiState.value.updateInfo?.downloadUrl
        if (url.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "APK download URL is missing from the release."
            )
            return
        }

        viewModelScope.launch {
            UpdateManager.downloadApk(context, url) { status ->
                _uiState.value = _uiState.value.copy(downloadStatus = status)

                if (status is DownloadStatus.Success) {
                    // Trigger installation flow
                    val installed = UpdateManager.installApk(context, status.apkFile)
                    if (!installed) {
                        _uiState.value = _uiState.value.copy(
                            infoMessage = "Grant permission to install unknown apps, then tap Install again."
                        )
                    }
                }
            }
        }
    }

    fun installDownloadedApk(context: Context, apkFile: File) {
        UpdateManager.installApk(context, apkFile)
    }

    fun dismissUpdateDialog() {
        _uiState.value = _uiState.value.copy(
            showUpdateDialog = false,
            downloadStatus = DownloadStatus.Idle
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            infoMessage = null,
            errorMessage = null
        )
    }
}
