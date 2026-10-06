package com.personalvault.di

import com.personalvault.data.repository.AuthRepositoryImpl
import com.personalvault.data.repository.DeviceRepositoryImpl
import com.personalvault.domain.repository.AuthRepository
import com.personalvault.domain.repository.DeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl
    ): DeviceRepository
}
