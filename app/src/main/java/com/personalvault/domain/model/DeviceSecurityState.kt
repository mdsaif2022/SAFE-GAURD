package com.personalvault.domain.model

data class DeviceSecurityState(
    val isEncryptedChannelActive: Boolean = true,
    val securityLevel: String = "High (AES-256)",
    val activeSessionsCount: Int = 1,
    val lastSecurityAudit: String = "Just now",
    val statusMessage: String = "Device connection is secure and private"
)
