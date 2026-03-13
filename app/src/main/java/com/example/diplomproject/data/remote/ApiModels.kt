package com.example.diplomproject.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    @SerialName("status") val status: String,
)
