package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface ResultProfileRepository : JpaRepository<ResultProfileEntity, UUID> {
    fun findBySessionId(sessionId: UUID): Optional<ResultProfileEntity>
}
