package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "test_categories")
class TestCategoryEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 64)
    val code: String,

    @Column(nullable = false, length = 255)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
