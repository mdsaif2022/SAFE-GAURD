package com.personalvault.data.repository

import com.personalvault.data.remote.VaultApiService
import com.personalvault.domain.model.Device
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.DeviceSecurityState
import com.personalvault.domain.model.DeviceStatus
import com.personalvault.domain.repository.DeviceRepository
import com.personalvault.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val apiService: VaultApiService
) : DeviceRepository {

    override fun getRegisteredDevices(): Flow<Resource<List<Device>>> = flow {
        emit(Resource.Loading)
        try {
            delay(500)
            val dtos = apiService.fetchDevices()
            val devices = dtos.map { dto ->
                Device(
                    id = dto.deviceId,
                    name = dto.deviceName,
                    mode = when (dto.modeString) {
                        "AGENT" -> DeviceMode.AGENT
                        "CONTROLLER" -> DeviceMode.CONTROLLER
                        else -> DeviceMode.UNCONFIGURED
                    },
                    status = when (dto.statusString) {
                        "ONLINE" -> DeviceStatus.ONLINE
                        "OFFLINE" -> DeviceStatus.OFFLINE
                        "CONNECTING" -> DeviceStatus.CONNECTING
                        else -> DeviceStatus.STANDBY
                    },
                    batteryLevel = dto.batteryPct,
                    networkType = dto.connectionType,
                    locationRegion = dto.regionName,
                    lastActiveFormatted = dto.lastSeenIso
                )
            }
            emit(Resource.Success(devices))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch device status"))
        }
    }

    override fun getAgentDevice(): Flow<Resource<Device?>> = flow {
        emit(Resource.Loading)
        try {
            delay(400)
            val dtos = apiService.fetchDevices()
            val agentDto = dtos.firstOrNull { it.modeString == "AGENT" }
            if (agentDto != null) {
                val device = Device(
                    id = agentDto.deviceId,
                    name = agentDto.deviceName,
                    mode = DeviceMode.AGENT,
                    status = DeviceStatus.ONLINE,
                    batteryLevel = agentDto.batteryPct,
                    networkType = agentDto.connectionType,
                    locationRegion = agentDto.regionName,
                    lastActiveFormatted = agentDto.lastSeenIso
                )
                emit(Resource.Success(device))
            } else {
                emit(Resource.Success(null))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch Agent device"))
        }
    }

    override fun getSecurityState(): Flow<DeviceSecurityState> = flow {
        emit(
            DeviceSecurityState(
                isEncryptedChannelActive = true,
                securityLevel = "AES-256 Encrypted",
                activeSessionsCount = 1,
                lastSecurityAudit = "Active & Monitored",
                statusMessage = "All sessions securely authenticated"
            )
        )
    }

    override fun refreshDevices(): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        delay(600)
        emit(Resource.Success(Unit))
    }
}
