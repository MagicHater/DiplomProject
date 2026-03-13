package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.RoleEntity
import com.example.adaptivetestingbackend.entity.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findByName(name: RoleName): Optional<RoleEntity>
}
