package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.QuestionOptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuestionOptionRepository : JpaRepository<QuestionOptionEntity, UUID> {
    fun findByQuestionIdOrderByOptionOrderAsc(questionId: UUID): List<QuestionOptionEntity>
}
