package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.AnswerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AnswerRepository : JpaRepository<AnswerEntity, UUID> {
    fun findBySessionId(sessionId: UUID): List<AnswerEntity>

    fun existsByQuestionSnapshotId(questionSnapshotId: UUID): Boolean
}
