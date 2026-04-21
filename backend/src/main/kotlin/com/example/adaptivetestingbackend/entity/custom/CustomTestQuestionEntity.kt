package com.example.adaptivetestingbackend.entity.custom

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "custom_test_questions")
class CustomTestQuestionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    val test: CustomTestEntity,

    @Column(name = "question_order", nullable = false)
    val questionOrder: Int,

    @Column(name = "text", nullable = false)
    val text: String,

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    val options: MutableList<CustomTestOptionEntity> = mutableListOf(),
)
