package com.personalvault.domain.model

data class DeviceProfile(
    val deviceId: String,
    val accountId: String,
    val deviceName: String,
    val deviceRole: DeviceMode,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
