package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "questions")
class QuestionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String,

    @Column(name = "scale_weights_json", nullable = false, columnDefinition = "TEXT")
    val scaleWeightsJson: String,

    @Column(nullable = false)
    val difficulty: Short = 1,

    @Column(nullable = false)
    val priority: Int = 0,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    val category: TestCategoryEntity,

    @OneToMany(mappedBy = "question")
    val options: MutableSet<QuestionOptionEntity> = mutableSetOf(),
)
