package com.example.adaptivetestingbackend.entity.custom

import com.example.adaptivetestingbackend.entity.UserEntity
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
@Table(name = "custom_test_submissions")
class CustomTestSubmissionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    val test: CustomTestEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
    val answers: MutableList<CustomTestSubmissionAnswerEntity> = mutableListOf(),
)
