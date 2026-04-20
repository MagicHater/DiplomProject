package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
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
@Table(name = "test_sessions")
class TestSessionEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_user_id")
    val candidate: UserEntity? = null,

    @Convert(converter = TestSessionStatusConverter::class)
    @Column(nullable = false, length = 32)
    var status: TestSessionStatus = TestSessionStatus.CREATED,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: TestCategoryEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_token_id")
    var accessToken: TestAccessTokenEntity? = null,

    @Column(name = "guest_identifier", length = 255)
    var guestIdentifier: String? = null,

    @Column(name = "guest_session_key", length = 128)
    var guestSessionKey: String? = null,

    @Column(name = "started_at")
    val startedAt: OffsetDateTime? = null,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: OffsetDateTime? = null,

    @Column(name = "adaptive_state_json", nullable = false, columnDefinition = "TEXT")
    var adaptiveStateJson: String = "{}",

    @OneToMany(mappedBy = "session")
    val questionSnapshots: MutableSet<QuestionSnapshotEntity> = mutableSetOf(),
)
