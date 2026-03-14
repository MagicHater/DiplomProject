package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.QuestionSnapshotEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuestionSnapshotRepository : JpaRepository<QuestionSnapshotEntity, UUID> {
    fun findBySessionIdOrderByQuestionOrderAsc(sessionId: UUID): List<QuestionSnapshotEntity>
}
