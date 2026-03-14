package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "result_profiles")
class ResultProfileEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    val session: TestSessionEntity,

    @Column(name = "attention_score", nullable = false, precision = 5, scale = 2)
    val attentionScore: BigDecimal,

    @Column(name = "stress_resistance_score", nullable = false, precision = 5, scale = 2)
    val stressResistanceScore: BigDecimal,

    @Column(name = "responsibility_score", nullable = false, precision = 5, scale = 2)
    val responsibilityScore: BigDecimal,

    @Column(name = "adaptability_score", nullable = false, precision = 5, scale = 2)
    val adaptabilityScore: BigDecimal,

    @Column(name = "decision_speed_accuracy_score", nullable = false, precision = 5, scale = 2)
    val decisionSpeedAccuracyScore: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val summary: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
)
