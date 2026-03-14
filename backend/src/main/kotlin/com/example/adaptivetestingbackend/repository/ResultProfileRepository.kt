package com.example.adaptivetestingbackend.repository

import com.example.adaptivetestingbackend.entity.ResultProfileEntity
import com.example.adaptivetestingbackend.entity.TestSessionStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface ResultProfileRepository : JpaRepository<ResultProfileEntity, UUID> {
    fun findBySessionId(sessionId: UUID): Optional<ResultProfileEntity>

    @Query(
        """
        select rp
        from ResultProfileEntity rp
        join fetch rp.session s
        where s.candidate.id = :candidateId
          and s.status = :status
        order by s.completedAt desc
        """,
    )
    fun findCompletedByCandidateIdOrderByCompletedAtDesc(
        @Param("candidateId") candidateId: UUID,
        @Param("status") status: TestSessionStatus,
    ): List<ResultProfileEntity>

    @Query(
        """
        select rp
        from ResultProfileEntity rp
        join fetch rp.session s
        join fetch s.candidate c
        where s.id = :sessionId
        """,
    )
    fun findBySessionIdWithCandidate(
        @Param("sessionId") sessionId: UUID,
    ): Optional<ResultProfileEntity>

    @Query(
        """
        select count(rp)
        from ResultProfileEntity rp
        join rp.session s
        where s.candidate.id = :candidateId
          and s.status = :status
        """,
    )
    fun countCompletedByCandidateId(
        @Param("candidateId") candidateId: UUID,
        @Param("status") status: TestSessionStatus,
    ): Long

    @Query(
        """
        select max(s.completedAt)
        from ResultProfileEntity rp
        join rp.session s
        where s.candidate.id = :candidateId
          and s.status = :status
        """,
    )
    fun findLastCompletedAtByCandidateId(
        @Param("candidateId") candidateId: UUID,
        @Param("status") status: TestSessionStatus,
    ): java.time.OffsetDateTime?
}
