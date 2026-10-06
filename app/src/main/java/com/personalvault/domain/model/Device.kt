package com.personalvault.domain.model

data class Device(
    val id: String,
    val name: String,
    val mode: DeviceMode,
    val status: DeviceStatus,
    val batteryLevel: Int,
    val networkType: String,
    val locationRegion: String,
    val lastActiveFormatted: String,
    val ipAddress: String = "192.168.1.100"
)

enum class DeviceStatus {
    ONLINE,
    OFFLINE,
    CONNECTING,
    STANDBY
}
