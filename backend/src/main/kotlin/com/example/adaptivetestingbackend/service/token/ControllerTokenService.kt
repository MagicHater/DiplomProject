package com.example.adaptivetestingbackend.service.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.testsession.ResultProfileResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenResultListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestAccessTokenEntity
import com.example.adaptivetestingbackend.repository.ResultProfileRepository
import com.example.adaptivetestingbackend.repository.TestAccessTokenRepository
import com.example.adaptivetestingbackend.repository.TestCategoryRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class ControllerTokenService(
    private val userRepository: UserRepository,
    private val testCategoryRepository: TestCategoryRepository,
    private val testAccessTokenRepository: TestAccessTokenRepository,
    private val resultProfileRepository: ResultProfileRepository,
    private val tokenGenerationService: TokenGenerationService,
    private val resultProfileMapper: com.example.adaptivetestingbackend.service.testsession.ResultProfileMapper,
) {
    @Transactional(readOnly = true)
    fun getActiveCategories(): List<TestCategoryResponse> =
        testCategoryRepository.findByIsActiveTrueOrderByNameAsc().map { it.toResponse() }

    @Transactional
    fun createToken(controllerEmail: String, categoryId: UUID): CreateControllerTokenResponse {
        val normalizedEmail = controllerEmail.trim().lowercase()

        println("CREATE_TOKEN_EMAIL_RAW = $controllerEmail")
        println("CREATE_TOKEN_EMAIL_NORMALIZED = $normalizedEmail")
        println("CREATE_TOKEN_CATEGORY_ID = $categoryId")

        val controller = userRepository.findByEmail(normalizedEmail)
            .orElseThrow {
                println("CREATE_TOKEN_USER_NOT_FOUND = $normalizedEmail")
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
            }

        println("CREATE_TOKEN_USER_FOUND = ${controller.email}")
        println("CREATE_TOKEN_USER_ROLE = ${controller.role.name}")

        if (controller.role.name != RoleName.CONTROLLER) {
            println("CREATE_TOKEN_FORBIDDEN_ROLE = ${controller.role.name}")
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can generate token")
        }

        val category = testCategoryRepository.findById(categoryId)
            .orElseThrow {
                println("CREATE_TOKEN_CATEGORY_NOT_FOUND = $categoryId")
                ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")
            }

        println("CREATE_TOKEN_CATEGORY_FOUND = ${category.id} / ${category.code}")

        val tokenValue = tokenGenerationService.generateUniqueToken(controller.id, category.code)
        println("CREATE_TOKEN_GENERATED = $tokenValue")

        val token = testAccessTokenRepository.save(
            TestAccessTokenEntity(
                token = tokenValue,
                createdBy = controller,
                category = category,
            ),
        )

        println("CREATE_TOKEN_SAVED_ID = ${token.id}")

        return CreateControllerTokenResponse(
            token = token.token,
            category = category.toResponse(),
            createdAt = token.createdAt,
            isUsed = token.isUsed,
        )
    }

    @Transactional(readOnly = true)
    fun getControllerTokenHistory(controllerEmail: String): List<ControllerTokenListItemResponse> {
        val controller = getControllerUser(controllerEmail)

        return testAccessTokenRepository.findTop20ByCreatedByOrderByCreatedAtDesc(controller)
            .map {
                ControllerTokenListItemResponse(
                    token = it.token,
                    category = it.category.toResponse(),
                    createdAt = it.createdAt,
                    isUsed = it.isUsed,
                    usedAt = it.usedAt,
                )
            }
    }

    @Transactional(readOnly = true)
    fun getControllerTokenResults(controllerEmail: String): List<ControllerTokenResultListItemResponse> {
        val controller = getControllerUser(controllerEmail)

        return resultProfileRepository.findCompletedByControllerIdOrderByCompletedAtDesc(
            controllerId = controller.id,
            status = com.example.adaptivetestingbackend.entity.TestSessionStatus.COMPLETED,
        ).map { profile ->
            val session = profile.session
            val candidate = session.candidate
            val participantType = if (candidate != null) "candidate" else "guest"
            val participantDisplayName = candidate?.email ?: session.guestIdentifier ?: session.accessToken?.usedByGuestDisplayName

            ControllerTokenResultListItemResponse(
                sessionId = session.id,
                completedAt = session.completedAt
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Session completedAt is missing"),
                category = session.category.toResponse(),
                participantType = participantType,
                participantDisplayName = participantDisplayName,
                summary = profile.summary ?: resultProfileMapper.buildOverallSummary(
                    com.example.adaptivetestingbackend.service.testsession.ResultCalculationService.CalculatedProfile(
                        attention = profile.attentionScore,
                        stressResistance = profile.stressResistanceScore,
                        responsibility = profile.responsibilityScore,
                        adaptability = profile.adaptabilityScore,
                        decisionSpeedAccuracy = profile.decisionSpeedAccuracyScore,
                    ),
                ),
                scores = com.example.adaptivetestingbackend.dto.testsession.ScaleScoresDto(
                    attention = profile.attentionScore,
                    stressResistance = profile.stressResistanceScore,
                    responsibility = profile.responsibilityScore,
                    adaptability = profile.adaptabilityScore,
                    decisionSpeedAccuracy = profile.decisionSpeedAccuracyScore,
                ),
            )
        }
    }

    @Transactional(readOnly = true)
    fun getControllerTokenResultBySessionId(controllerEmail: String, sessionId: UUID): ResultProfileResponse {
        val controller = getControllerUser(controllerEmail)

        val profile = resultProfileRepository.findCompletedBySessionIdAndControllerId(
            sessionId = sessionId,
            controllerId = controller.id,
            status = com.example.adaptivetestingbackend.entity.TestSessionStatus.COMPLETED,
        ).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Result not found") }

        return resultProfileMapper.toResultProfile(profile)
    }

    private fun getControllerUser(controllerEmail: String): com.example.adaptivetestingbackend.entity.UserEntity {
        val normalizedEmail = controllerEmail.trim().lowercase()
        val controller = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (controller.role.name != RoleName.CONTROLLER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can access token management")
        }

        return controller
    }

}

private fun com.example.adaptivetestingbackend.entity.TestCategoryEntity.toResponse() = TestCategoryResponse(
    id = id,
    code = code,
    name = name,
    description = description,
)