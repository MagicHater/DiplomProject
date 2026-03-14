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
class AuthControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun `register login and me flow works`() {
        val registerBody = mapOf(
            "fullName" to "Ivan Petrov",
            "email" to "ivan@example.com",
            "password" to "password123",
            "role" to "candidate",
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerBody)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.email") { value("ivan@example.com") }
                jsonPath("$.role") { value("candidate") }
            }

        val loginBody = mapOf(
            "email" to "ivan@example.com",
            "password" to "password123",
        )

        val token = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginBody)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.token") { isNotEmpty() }
            }
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readTree(it).get("token").asText() }

        mockMvc.get("/auth/me") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.email") { value("ivan@example.com") }
                jsonPath("$.role") { value("candidate") }
            }
    }

    @Test
    fun `me without token returns unauthorized`() {
        mockMvc.get("/auth/me")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `login with wrong password returns unauthorized`() {
        val registerBody = mapOf(
            "fullName" to "Olga Ivanova",
            "email" to "olga@example.com",
            "password" to "correct-password",
            "role" to "candidate",
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerBody)
        }
            .andExpect {
                status { isOk() }
            }

        val loginBody = mapOf(
            "email" to "olga@example.com",
            "password" to "wrong-password",
        )

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginBody)
        }
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
