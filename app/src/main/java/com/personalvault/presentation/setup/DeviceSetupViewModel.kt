package com.personalvault.presentation.setup

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalvault.data.local.UserPreferencesRepository
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.DeviceProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import javax.inject.Inject

data class DeviceSetupUiState(
    val selectedMode: DeviceMode = DeviceMode.AGENT,
    val showConfirmationDialog: Boolean = false,
    val pendingMode: DeviceMode? = null,
    val isLoading: Boolean = false,
    val deviceProfile: DeviceProfile? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DeviceSetupViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceSetupUiState())
    val uiState: StateFlow<DeviceSetupUiState> = _uiState.asStateFlow()

    init {
        val existingProfile = preferencesRepository.getDeviceProfile()
        if (existingProfile != null) {
            _uiState.value = _uiState.value.copy(
                selectedMode = existingProfile.deviceRole,
                deviceProfile = existingProfile
            )
        }
    }

    fun selectMode(mode: DeviceMode) {
        _uiState.value = _uiState.value.copy(
            selectedMode = mode,
            errorMessage = null
        )
    }

    fun requestConfirmation(mode: DeviceMode = _uiState.value.selectedMode) {
        _uiState.value = _uiState.value.copy(
            pendingMode = mode,
            showConfirmationDialog = true
        )
    }

    fun dismissConfirmationDialog() {
        _uiState.value = _uiState.value.copy(
            showConfirmationDialog = false,
            pendingMode = null
        )
    }

    fun confirmRoleRegistration(onComplete: (DeviceMode) -> Unit) {
        val targetRole = _uiState.value.pendingMode ?: _uiState.value.selectedMode
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            showConfirmationDialog = false
        )

        viewModelScope.launch {
            try {
                val currentSession = preferencesRepository.session.value
                val accountId = currentSession?.userId?.ifBlank { "acc_local_owner" } ?: "acc_local_owner"
                val rawModel = Build.MODEL
                val deviceName = if (rawModel.isNullOrBlank()) "Personal Phone" else rawModel

                // Cryptographically secure random device ID generation
                // Avoids Android hardware serial numbers, IMEIs, phone numbers, and MAC addresses.
                val secureDeviceId = generateSecureDeviceId()

                val profile = DeviceProfile(
                    deviceId = secureDeviceId,
                    accountId = accountId,
                    deviceName = deviceName,
                    deviceRole = targetRole,
                    createdAt = System.currentTimeMillis(),
                    lastSeenAt = System.currentTimeMillis(),
                    isActive = true
                )

                // Persist device profile locally using secure storage
                preferencesRepository.saveDeviceProfile(profile)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedMode = targetRole,
                    deviceProfile = profile
                )

                onComplete(targetRole)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to save device profile"
                )
            }
        }
    }

    private fun generateSecureDeviceId(): String {
        val randomBytes = ByteArray(16)
        SecureRandom().nextBytes(randomBytes)
        val hex = randomBytes.joinToString("") { "%02x".format(it) }
        return "dev_$hex"
    }
}
