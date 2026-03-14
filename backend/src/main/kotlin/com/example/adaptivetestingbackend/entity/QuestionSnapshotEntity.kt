package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "question_snapshots")
class QuestionSnapshotEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    val session: TestSessionEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    val question: QuestionEntity? = null,

    @Column(name = "question_order", nullable = false)
    val questionOrder: Int,

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    val questionText: String,

    @Column(name = "scale_weights_json", nullable = false, columnDefinition = "TEXT")
    val scaleWeightsJson: String,

    @Column(nullable = false)
    val difficulty: Short,

    @Column(nullable = false)
    val priority: Int,

    @Column(name = "option_snapshots_json", columnDefinition = "TEXT")
    val optionSnapshotsJson: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
)
