package com.example.adaptivetestingbackend

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTokenManagementControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun `controller can generate token through secured endpoint`() {
        val controllerToken = registerAndLogin(email = "controller-token-1@example.com", role = "controller")

        val categoriesResponse = mockMvc.get("/controller/test-management/categories") {
            header("Authorization", "Bearer $controllerToken")
        }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString

        val categoryId = objectMapper.readTree(categoriesResponse).first().get("id").asText()

        val requestBody = mapOf("categoryId" to categoryId)

        mockMvc.post("/controller/test-management/tokens") {
            header("Authorization", "Bearer $controllerToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestBody)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.token") { isNotEmpty() }
                jsonPath("$.category.id") { value(categoryId) }
                jsonPath("$.createdAt") { exists() }
                jsonPath("$.isUsed") { value(false) }
            }
    }

    @Test
    fun `controller token generation requires authorization`() {
        val categoriesResponse = mockMvc.get("/test-categories")
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString

        val categoryId = objectMapper.readTree(categoriesResponse).first().get("id").asText()

        val requestBody = mapOf("categoryId" to categoryId)

        mockMvc.post("/controller/test-management/tokens") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(requestBody)
        }
            .andExpect { status { isUnauthorized() } }
    }

    private fun registerAndLogin(email: String, role: String): String {
        val registerBody = mapOf(
            "fullName" to "Controller User",
            "email" to email,
            "password" to "password123",
            "role" to role,
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerBody)
        }
            .andExpect { status { isOk() } }

        val loginBody = mapOf("email" to email, "password" to "password123")

        return mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginBody)
        }
            .andExpect { status { isOk() } }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("token").asText() }
    }
}
