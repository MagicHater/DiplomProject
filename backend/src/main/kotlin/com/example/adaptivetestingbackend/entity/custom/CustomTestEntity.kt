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
@Table(name = "custom_tests")
class CustomTestEntity(
    @Id
    @Column(nullable = false)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "controller_id", nullable = false)
    val controller: UserEntity,

    @Column(name = "title", nullable = false, length = 255)
    val title: String,

    @Column(name = "description")
    val description: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY)
    val questions: MutableList<CustomTestQuestionEntity> = mutableListOf(),

    @OneToMany(mappedBy = "test", fetch = FetchType.LAZY)
    val allowedEmails: MutableList<CustomTestAllowedEmailEntity> = mutableListOf(),
)
