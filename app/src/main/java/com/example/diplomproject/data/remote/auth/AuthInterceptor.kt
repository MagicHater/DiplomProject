package com.example.diplomproject.data.remote.auth

import com.example.diplomproject.data.local.SessionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.shouldSkipAuthHeader()) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { sessionManager.tokenFlow.firstOrNull() }
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authorizedRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authorizedRequest)
    }
}

private fun okhttp3.Request.shouldSkipAuthHeader(): Boolean {
    val path = url.encodedPath
    return path == "/auth/login" || path == "/auth/register"
}
