package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.entity.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>

    fun existsByEmail(email: String): Boolean

    @Query(
        """
        select u
        from UserEntity u
        where u.role.name = :candidateRole
        order by u.fullName asc, u.createdAt asc
        """,
    )
    fun findCandidatesWithAccountOrResults(
        @Param("candidateRole") candidateRole: RoleName,
    ): List<UserEntity>
}
