package com.example.adaptivetestingbackend.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "roles")
class RoleEntity(
    @Id
    @Column(nullable = false)
    val id: UUID,

    @Convert(converter = RoleNameConverter::class)
    @Column(nullable = false, unique = true, length = 32)
    val name: RoleName,

    @OneToMany(mappedBy = "role")
    val users: MutableSet<UserEntity> = mutableSetOf(),
)
