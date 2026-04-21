package com.example.adaptivetestingbackend.entity.custom

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "custom_test_options")
class CustomTestOptionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    val question: CustomTestQuestionEntity,

    @Column(name = "option_order", nullable = false)
    val optionOrder: Int,

    @Column(name = "text", nullable = false)
    val text: String,
)
