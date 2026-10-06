package com.personalvault.data.remote

import com.personalvault.data.model.DeviceDto
import com.personalvault.data.model.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultApiService @Inject constructor() {

    suspend fun loginUser(email: String, pass: String): UserDto {
        return UserDto(
            id = "usr_882910",
            email = email,
            name = "Personal Account Owner",
            token = "jwt_sec_personal_vault_${System.currentTimeMillis()}"
        )
    }

    suspend fun registerUser(email: String, pass: String, name: String): UserDto {
        return UserDto(
            id = "usr_${System.currentTimeMillis() % 100000}",
            email = email,
            name = name,
            token = "jwt_sec_personal_vault_${System.currentTimeMillis()}"
        )
    }

    suspend fun fetchDevices(): List<DeviceDto> {
        return listOf(
            DeviceDto(
                deviceId = "dev_agent_01",
                deviceName = "Home Country Agent (Pixel 7)",
                modeString = "AGENT",
                statusString = "ONLINE",
                batteryPct = 94,
                connectionType = "Wi-Fi 6 (Home Network)",
                regionName = "Home Country",
                lastSeenIso = "Connected 2m ago"
            ),
            DeviceDto(
                deviceId = "dev_controller_01",
                deviceName = "Travel Controller (Galaxy S24)",
                modeString = "CONTROLLER",
                statusString = "ONLINE",
                batteryPct = 81,
                connectionType = "5G Cellular Roaming",
                regionName = "Abroad (Active)",
                lastSeenIso = "Active Now"
            )
        )
    }
}
