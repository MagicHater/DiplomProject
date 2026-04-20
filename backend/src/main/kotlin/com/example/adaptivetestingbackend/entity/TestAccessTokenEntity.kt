package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "test_access_tokens")
class TestAccessTokenEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 128)
    val token: String,

    @Column(name = "is_used", nullable = false)
    var isUsed: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    val createdBy: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    val category: TestCategoryEntity,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "used_at")
    var usedAt: OffsetDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id")
    var usedByUser: UserEntity? = null,

    @Column(name = "used_by_guest_display_name", length = 255)
    var usedByGuestDisplayName: String? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id")
    var testSession: TestSessionEntity? = null,
)
