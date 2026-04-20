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
    fun createToken(controllerEmail: String, categoryId: java.util.UUID): CreateControllerTokenResponse {
        val controller = userRepository.findByEmail(controllerEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        if (controller.role.name != RoleName.CONTROLLER) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only controller can generate token")
        }

        val category = testCategoryRepository.findById(categoryId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found") }

        val tokenValue = tokenGenerationService.generateUniqueToken(controller.id, category.code)
        val token = testAccessTokenRepository.save(
            TestAccessTokenEntity(
                token = tokenValue,
                createdBy = controller,
                category = category,
            ),
        )

        return CreateControllerTokenResponse(
            token = token.token,
            category = category.toResponse(),
            createdAt = token.createdAt,
            isUsed = token.isUsed,
        )
    }

    @Transactional(readOnly = true)
    fun getControllerTokenHistory(controllerEmail: String): List<ControllerTokenListItemResponse> {
        val controller = userRepository.findByEmail(controllerEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

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
