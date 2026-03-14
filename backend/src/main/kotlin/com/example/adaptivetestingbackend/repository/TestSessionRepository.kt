package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.TestSessionEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TestSessionRepository : JpaRepository<TestSessionEntity, UUID> {
    fun findByCandidateIdOrderByCreatedAtDesc(candidateId: UUID): List<TestSessionEntity>

    fun findByCandidateIdAndStatus(candidateId: UUID, status: TestSessionStatus): List<TestSessionEntity>
}
