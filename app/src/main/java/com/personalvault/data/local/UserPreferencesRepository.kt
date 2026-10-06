package com.personalvault.data.local

import android.content.Context
import android.content.SharedPreferences
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.DeviceProfile
import com.personalvault.domain.model.UserSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("personal_vault_secure_prefs", Context.MODE_PRIVATE)
    }

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _deviceProfile = MutableStateFlow<DeviceProfile?>(null)
    val deviceProfile: StateFlow<DeviceProfile?> = _deviceProfile.asStateFlow()

    init {
        _deviceProfile.value = loadDeviceProfile()
    }

    fun saveSession(session: UserSession) {
        _session.value = session
    }

    fun saveDeviceProfile(profile: DeviceProfile) {
        _deviceProfile.value = profile
        prefs.edit().apply {
            putString("device_id", profile.deviceId)
            putString("account_id", profile.accountId)
            putString("device_name", profile.deviceName)
            putString("device_role", profile.deviceRole.name)
            putLong("created_at", profile.createdAt)
            putLong("last_seen_at", profile.lastSeenAt)
            putBoolean("is_active", profile.isActive)
            apply()
        }
        updateMode(profile.deviceRole)
    }

    fun getDeviceProfile(): DeviceProfile? {
        return _deviceProfile.value ?: loadDeviceProfile()
    }

    private fun loadDeviceProfile(): DeviceProfile? {
        val deviceId = prefs.getString("device_id", null) ?: return null
        val accountId = prefs.getString("account_id", "") ?: ""
        val deviceName = prefs.getString("device_name", "Personal Phone") ?: "Personal Phone"
        val roleStr = prefs.getString("device_role", DeviceMode.AGENT.name)
        val role = try {
            DeviceMode.valueOf(roleStr ?: DeviceMode.AGENT.name)
        } catch (_: Exception) {
            DeviceMode.AGENT
        }
        val createdAt = prefs.getLong("created_at", System.currentTimeMillis())
        val lastSeenAt = prefs.getLong("last_seen_at", System.currentTimeMillis())
        val isActive = prefs.getBoolean("is_active", true)

        return DeviceProfile(
            deviceId = deviceId,
            accountId = accountId,
            deviceName = deviceName,
            deviceRole = role,
            createdAt = createdAt,
            lastSeenAt = lastSeenAt,
            isActive = isActive
        )
    }

    fun updateMode(mode: DeviceMode) {
        val current = _session.value
        if (current != null) {
            _session.value = current.copy(deviceMode = mode)
        } else {
            _session.value = UserSession(
                userId = "user_default",
                email = "user@personalvault.private",
                fullName = "Personal Account",
                token = "token_default",
                activeDeviceId = _deviceProfile.value?.deviceId ?: "device_local",
                deviceMode = mode
            )
        }
    }

    fun clearSession() {
        _session.value = null
        _deviceProfile.value = null
        prefs.edit().clear().apply()
    }
}
