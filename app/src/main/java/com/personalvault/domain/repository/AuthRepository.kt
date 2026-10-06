package com.personalvault.domain.repository

import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.UserSession
import com.personalvault.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, pass: String): Flow<Resource<UserSession>>
    fun createAccount(email: String, pass: String, fullName: String): Flow<Resource<UserSession>>
    fun getCurrentSession(): Flow<UserSession?>
    fun updateDeviceMode(mode: DeviceMode): Flow<Resource<UserSession>>
    suspend fun logout()
}
