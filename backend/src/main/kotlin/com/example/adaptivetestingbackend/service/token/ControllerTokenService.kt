package com.example.adaptivetestingbackend.service.token

import com.example.adaptivetestingbackend.dto.testsession.TestCategoryResponse
import com.example.adaptivetestingbackend.dto.token.ControllerTokenListItemResponse
import com.example.adaptivetestingbackend.dto.token.CreateControllerTokenResponse
import com.example.adaptivetestingbackend.entity.RoleName
import com.example.adaptivetestingbackend.entity.TestAccessTokenEntity
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
    private val tokenGenerationService: TokenGenerationService,
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
        val normalizedEmail = controllerEmail.trim().lowercase()

        println("GET_CONTROLLER_TOKENS_EMAIL_RAW = $controllerEmail")
        println("GET_CONTROLLER_TOKENS_EMAIL_NORMALIZED = $normalizedEmail")

        val controller = userRepository.findByEmail(normalizedEmail)
            .orElseThrow {
                println("GET_CONTROLLER_TOKENS_USER_NOT_FOUND = $normalizedEmail")
                ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found")
            }

        println("GET_CONTROLLER_TOKENS_USER_FOUND = ${controller.email}")
        println("GET_CONTROLLER_TOKENS_USER_ROLE = ${controller.role.name}")

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
}

private fun com.example.adaptivetestingbackend.entity.TestCategoryEntity.toResponse() = TestCategoryResponse(
    id = id,
    code = code,
    name = name,
    description = description,
)