package com.personalvault.presentation.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalvault.data.local.UserPreferencesRepository
import com.personalvault.domain.model.Device
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.repository.DeviceRepository
import com.personalvault.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ControllerUiState(
    val accountEmail: String = "owner@personalvault.private",
    val devices: List<Device> = emptyList(),
    val agentDevice: Device? = null,
    val selectedFeature: String? = null,
    val showAddDeviceDialog: Boolean = false,
    val isLoading: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionStatusMessage: String = "Ready for remote connection",
    val errorMessage: String? = null
)

@HiltViewModel
class ControllerViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControllerUiState())
    val uiState: StateFlow<ControllerUiState> = _uiState.asStateFlow()

    init {
        observeLocalSession()
        loadDevices()
    }

    private fun observeLocalSession() {
        preferencesRepository.session
            .onEach { session ->
                if (session != null) {
                    _uiState.value = _uiState.value.copy(
                        accountEmail = session.email
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun loadDevices() {
        deviceRepository.getRegisteredDevices()
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is Resource.Success -> {
                        val profile = preferencesRepository.getDeviceProfile()
                        val rawList = result.data
                        val agent = rawList.firstOrNull { it.mode == DeviceMode.AGENT }
                            ?: profile?.let { prof ->
                                Device(
                                    id = prof.deviceId,
                                    name = prof.deviceName,
                                    mode = prof.deviceRole,
                                    status = com.personalvault.domain.model.DeviceStatus.ONLINE,
                                    batteryLevel = 98,
                                    networkType = "Wi-Fi (Home Router)",
                                    locationRegion = "Home Country",
                                    lastActiveFormatted = "Active Now"
                                )
                            }

                        _uiState.value = _uiState.value.copy(
                            devices = rawList,
                            agentDevice = agent,
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun testAgentConnection() {
        _uiState.value = _uiState.value.copy(
            isConnecting = true,
            connectionStatusMessage = "Testing secure link to Home Agent..."
        )

        deviceRepository.refreshDevices()
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            connectionStatusMessage = "Secure channel verified! Home Agent is reachable."
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isConnecting = false,
                            connectionStatusMessage = "Connection failed: ${result.message}"
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSelectFeature(featureName: String) {
        _uiState.value = _uiState.value.copy(selectedFeature = featureName)
    }

    fun dismissFeatureDialog() {
        _uiState.value = _uiState.value.copy(selectedFeature = null)
    }

    fun onAddDeviceClicked() {
        _uiState.value = _uiState.value.copy(showAddDeviceDialog = true)
    }

    fun dismissAddDeviceDialog() {
        _uiState.value = _uiState.value.copy(showAddDeviceDialog = false)
    }
}
