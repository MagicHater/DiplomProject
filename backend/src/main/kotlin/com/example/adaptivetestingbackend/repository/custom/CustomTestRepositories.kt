package com.example.adaptivetestingbackend.repository.custom

import com.example.adaptivetestingbackend.entity.custom.CustomTestAllowedEmailEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestOptionEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestQuestionEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestSubmissionAnswerEntity
import com.example.adaptivetestingbackend.entity.custom.CustomTestSubmissionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CustomTestRepository : JpaRepository<CustomTestEntity, UUID> {
    fun findAllByControllerIdOrderByCreatedAtDesc(controllerId: UUID): List<CustomTestEntity>

    @Query(
        """
        select distinct t from CustomTestEntity t
        join t.allowedEmails ae
        where ae.email = :email
        order by t.createdAt desc
        """,
    )
    fun findAvailableForEmail(@Param("email") email: String): List<CustomTestEntity>
}

interface CustomTestQuestionRepository : JpaRepository<CustomTestQuestionEntity, UUID> {
    fun findAllByTestIdOrderByQuestionOrderAsc(testId: UUID): List<CustomTestQuestionEntity>
}

interface CustomTestOptionRepository : JpaRepository<CustomTestOptionEntity, UUID> {
    fun findAllByQuestionIdInOrderByOptionOrderAsc(questionIds: Collection<UUID>): List<CustomTestOptionEntity>
}

interface CustomTestAllowedEmailRepository : JpaRepository<CustomTestAllowedEmailEntity, UUID> {
    fun findAllByTestId(testId: UUID): List<CustomTestAllowedEmailEntity>
    fun existsByTestIdAndEmail(testId: UUID, email: String): Boolean
}

interface CustomTestSubmissionRepository : JpaRepository<CustomTestSubmissionEntity, UUID> {
    fun countByTestId(testId: UUID): Long
    fun findAllByTestIdOrderBySubmittedAtDesc(testId: UUID): List<CustomTestSubmissionEntity>
}

interface CustomTestSubmissionAnswerRepository : JpaRepository<CustomTestSubmissionAnswerEntity, UUID> {
    fun findAllBySubmissionIdIn(submissionIds: Collection<UUID>): List<CustomTestSubmissionAnswerEntity>

    @Query(
        """
        select a.question.id, a.option.id, count(a.id)
        from CustomTestSubmissionAnswerEntity a
        where a.submission.test.id = :testId
        group by a.question.id, a.option.id
        """,
    )
    fun aggregateOptionSelection(@Param("testId") testId: UUID): List<Array<Any>>
}
