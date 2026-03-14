package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.QuestionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuestionRepository : JpaRepository<QuestionEntity, UUID> {
    fun findByIsActiveTrue(): List<QuestionEntity>
}
