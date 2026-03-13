package com.example.diplomproject.data.repository

import com.example.diplomproject.domain.model.UserRole
import com.example.diplomproject.domain.repository.RoleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoleRepositoryImpl @Inject constructor() : RoleRepository {
    override fun getAvailableRoles(): List<UserRole> = listOf(
        UserRole.Candidate,
        UserRole.Controller,
    )
}
