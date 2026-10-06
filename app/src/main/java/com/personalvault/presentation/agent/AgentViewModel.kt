package com.personalvault.presentation.agent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalvault.data.local.UserPreferencesRepository
import com.personalvault.domain.model.Device
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.DeviceProfile
import com.personalvault.domain.model.DeviceSecurityState
import com.personalvault.domain.model.DeviceStatus
import com.personalvault.domain.model.UserSession
import com.personalvault.domain.repository.DeviceRepository
import com.personalvault.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AgentUiState(
    val deviceProfile: DeviceProfile? = null,
    val session: UserSession? = null,
    val agentDevice: Device = Device(
        id = "dev_agent_local",
        name = "Home Agent Phone",
        mode = DeviceMode.AGENT,
        status = DeviceStatus.ONLINE,
        batteryLevel = 98,
        networkType = "Wi-Fi 6 (Home Router)",
        locationRegion = "Home Country",
        lastActiveFormatted = "Active Now"
    ),
    val securityState: DeviceSecurityState = DeviceSecurityState(),
    val serviceStatus: String = "Active & Listening",
    val lastSyncTime: String = "Just now",
    val backendConnectionStatus: String = "Connected (Zero Telemetry)",
    val batteryLevelText: String = "98% (Charging)",
    val networkStatusText: String = "Wi-Fi (Home Fiber 5GHz)",
    val permissionsConfiguredText: String = "0 / 10 Permissions Configured (Placeholder Mode)",
    val accountStatusText: String = "Authenticated (Local Master Account)",
    val deviceRegistrationStatusText: String = "Registered Agent (Local Vault)",
    val isLoading: Boolean = false,
    val lastPingMessage: String = "Listening for Controller connections..."
)

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    init {
        observeLocalData()
        loadAgentData()
    }

    private fun observeLocalData() {
        preferencesRepository.deviceProfile
            .onEach { profile ->
                if (profile != null) {
                    val updatedDevice = _uiState.value.agentDevice.copy(
                        id = profile.deviceId,
                        name = profile.deviceName,
                        mode = profile.deviceRole
                    )
                    val shortId = if (profile.deviceId.length > 12) {
                        "${profile.deviceId.take(12)}..."
                    } else {
                        profile.deviceId
                    }
                    _uiState.value = _uiState.value.copy(
                        deviceProfile = profile,
                        agentDevice = updatedDevice,
                        deviceRegistrationStatusText = "Registered Agent ($shortId)"
                    )
                }
            }
            .launchIn(viewModelScope)

        preferencesRepository.session
            .onEach { session ->
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        session = session,
                        accountStatusText = "Authenticated (${session.email})"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadAgentData() {
        deviceRepository.getAgentDevice()
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val profile = _uiState.value.deviceProfile
                        val fetchedDevice = result.data ?: _uiState.value.agentDevice
                        val mergedDevice = fetchedDevice.copy(
                            id = profile?.deviceId ?: fetchedDevice.id,
                            name = profile?.deviceName ?: fetchedDevice.name,
                            mode = profile?.deviceRole ?: fetchedDevice.mode
                        )
                        _uiState.value = _uiState.value.copy(
                            agentDevice = mergedDevice,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            }
            .launchIn(viewModelScope)

        deviceRepository.getSecurityState()
            .onEach { sec ->
                _uiState.value = _uiState.value.copy(securityState = sec)
            }
            .launchIn(viewModelScope)
    }

    fun sendHeartbeatPing() {
        _uiState.value = _uiState.value.copy(
            lastPingMessage = "Heartbeat sent at ${System.currentTimeMillis() % 100000}ms",
            lastSyncTime = "Just now"
        )
    }
}
