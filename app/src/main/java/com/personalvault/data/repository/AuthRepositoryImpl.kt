package com.personalvault.data.repository

import com.personalvault.data.local.UserPreferencesRepository
import com.personalvault.data.remote.VaultApiService
import com.personalvault.domain.model.DeviceMode
import com.personalvault.domain.model.UserSession
import com.personalvault.domain.repository.AuthRepository
import com.personalvault.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: VaultApiService,
    private val preferencesRepository: UserPreferencesRepository
) : AuthRepository {

    override fun login(email: String, pass: String): Flow<Resource<UserSession>> = flow {
        emit(Resource.Loading)
        try {
            delay(800) // simulate secure network roundtrip
            val userDto = apiService.loginUser(email, pass)
            val session = UserSession(
                userId = userDto.id,
                email = userDto.email,
                fullName = userDto.name,
                token = userDto.token,
                activeDeviceId = "dev_this_phone",
                deviceMode = preferencesRepository.session.value?.deviceMode ?: DeviceMode.UNCONFIGURED
            )
            preferencesRepository.saveSession(session)
            emit(Resource.Success(session))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Authentication failed"))
        }
    }

    override fun createAccount(email: String, pass: String, fullName: String): Flow<Resource<UserSession>> = flow {
        emit(Resource.Loading)
        try {
            delay(1000)
            val userDto = apiService.registerUser(email, pass, fullName)
            val session = UserSession(
                userId = userDto.id,
                email = userDto.email,
                fullName = userDto.name,
                token = userDto.token,
                activeDeviceId = "dev_this_phone",
                deviceMode = DeviceMode.UNCONFIGURED
            )
            preferencesRepository.saveSession(session)
            emit(Resource.Success(session))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Account registration failed"))
        }
    }

    override fun getCurrentSession(): Flow<UserSession?> {
        return preferencesRepository.session
    }

    override fun updateDeviceMode(mode: DeviceMode): Flow<Resource<UserSession>> = flow {
        emit(Resource.Loading)
        try {
            delay(300)
            preferencesRepository.updateMode(mode)
            val updated = preferencesRepository.session.value ?: UserSession(
                userId = "usr_local",
                email = "owner@personalvault.private",
                fullName = "Personal Owner",
                token = "local_token",
                activeDeviceId = "dev_this_phone",
                deviceMode = mode
            )
            emit(Resource.Success(updated))
        } catch (e: Exception) {
            emit(Resource.Error("Failed to set device mode: ${e.message}"))
        }
    }

    override suspend fun logout() {
        preferencesRepository.clearSession()
    }
}
