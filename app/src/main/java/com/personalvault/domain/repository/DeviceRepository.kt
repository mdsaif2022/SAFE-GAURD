package com.personalvault.domain.repository

import com.personalvault.domain.model.Device
import com.personalvault.domain.model.DeviceSecurityState
import com.personalvault.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getRegisteredDevices(): Flow<Resource<List<Device>>>
    fun getAgentDevice(): Flow<Resource<Device?>>
    fun getSecurityState(): Flow<DeviceSecurityState>
    fun refreshDevices(): Flow<Resource<Unit>>
}
