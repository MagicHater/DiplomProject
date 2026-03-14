package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "answers")
class AnswerEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    val session: TestSessionEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_snapshot_id", nullable = false)
    val questionSnapshot: QuestionSnapshotEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    val selectedOption: QuestionOptionEntity? = null,

    @Column(name = "answer_value", precision = 6, scale = 2)
    val answerValue: BigDecimal? = null,

    @Column(name = "answered_at", nullable = false)
    val answeredAt: OffsetDateTime = OffsetDateTime.now(),
)
