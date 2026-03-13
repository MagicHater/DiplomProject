package com.example.diplomproject.di

import com.example.diplomproject.data.repository.AuthRepositoryImpl
import com.example.diplomproject.data.repository.RoleRepositoryImpl
import com.example.diplomproject.domain.repository.AuthRepository
import com.example.diplomproject.domain.repository.RoleRepository
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
    abstract fun bindRoleRepository(impl: RoleRepositoryImpl): RoleRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
