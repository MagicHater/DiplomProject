package com.example.diplomproject.data.remote

import retrofit2.http.GET

interface AppApi {
    @GET("health")
    suspend fun getHealth(): HealthResponse
}
