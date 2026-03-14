package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "question_options")
class QuestionOptionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    val question: QuestionEntity,

    @Column(name = "option_order", nullable = false)
    val optionOrder: Short,

    @Column(name = "option_text", nullable = false, length = 500)
    val optionText: String,

    @Column(name = "contribution_value", nullable = false, precision = 6, scale = 2)
    val contributionValue: BigDecimal,

    @Column(name = "scale_contributions_json", columnDefinition = "TEXT")
    val scaleContributionsJson: String? = null,
)
