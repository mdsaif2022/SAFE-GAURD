package com.personalvault.domain.model

data class UserSession(
    val userId: String,
    val email: String,
    val fullName: String,
    val token: String,
    val activeDeviceId: String,
    val deviceMode: DeviceMode = DeviceMode.UNCONFIGURED
)

enum class DeviceMode {
    AGENT,
    CONTROLLER,
    UNCONFIGURED
}
