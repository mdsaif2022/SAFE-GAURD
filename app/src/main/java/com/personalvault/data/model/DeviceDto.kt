package com.personalvault.data.model

data class DeviceDto(
    val deviceId: String,
    val deviceName: String,
    val modeString: String,
    val statusString: String,
    val batteryPct: Int,
    val connectionType: String,
    val regionName: String,
    val lastSeenIso: String
)
