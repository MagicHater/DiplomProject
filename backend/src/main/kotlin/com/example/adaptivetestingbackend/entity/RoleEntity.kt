package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    @Column(nullable = false)
    val id: UUID,

    @Column(nullable = false, unique = true)
    val name: String,
)
