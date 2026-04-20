package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.TestAccessTokenEntity
import com.example.adaptivetestingbackend.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TestAccessTokenRepository : JpaRepository<TestAccessTokenEntity, UUID> {
    fun findByToken(token: String): Optional<TestAccessTokenEntity>

    fun existsByToken(token: String): Boolean

    fun findByTokenAndIsUsedFalse(token: String): TestAccessTokenEntity?

    fun findTop20ByCreatedByOrderByCreatedAtDesc(createdBy: UserEntity): List<TestAccessTokenEntity>
}
