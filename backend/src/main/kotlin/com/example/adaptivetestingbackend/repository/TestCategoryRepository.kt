package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.TestCategoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface TestCategoryRepository : JpaRepository<TestCategoryEntity, UUID> {
    fun findByCode(code: String): Optional<TestCategoryEntity>

    fun findByIsActiveTrueOrderByNameAsc(): List<TestCategoryEntity>

    fun findFirstByIsActiveTrueOrderByNameAsc(): TestCategoryEntity?
}
