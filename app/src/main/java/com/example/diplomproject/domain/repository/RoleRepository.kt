package com.example.diplomproject.domain.repository

import com.example.diplomproject.domain.model.UserRole

interface RoleRepository {
    fun getAvailableRoles(): List<UserRole>
}
