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
        var requestBuilder = originalRequest.newBuilder()

        val path = originalRequest.url.encodedPath
        val explicitAuthHeader = originalRequest.header("Authorization")
        val explicitGuestHeader = originalRequest.header("X-Guest-Session-Key")

        val guestSessionKey = if (originalRequest.shouldAttachGuestSessionHeader() && explicitGuestHeader.isNullOrBlank()) {
            runBlocking { sessionManager.guestSessionKeyFlow.firstOrNull() }
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        } else {
            explicitGuestHeader
        }

        val requestUsesGuestSession = !guestSessionKey.isNullOrBlank() && originalRequest.shouldAttachGuestSessionHeader()

        if (!guestSessionKey.isNullOrBlank() && originalRequest.shouldAttachGuestSessionHeader()) {
            requestBuilder = requestBuilder.header("X-Guest-Session-Key", guestSessionKey)
        }

        val shouldAttachAuth =
            !originalRequest.shouldSkipAuthHeader() &&
                    explicitAuthHeader.isNullOrBlank() &&
                    !requestUsesGuestSession

        if (shouldAttachAuth) {
            val token = runBlocking { sessionManager.tokenFlow.firstOrNull() }.normalizeJwtToken()
            if (!token.isNullOrBlank()) {
                requestBuilder = requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}

private fun okhttp3.Request.shouldSkipAuthHeader(): Boolean {
    val path = url.encodedPath
    return path == "/auth/login" ||
            path == "/auth/register" ||
            path == "/token-access/preview" ||
            path == "/token-access/start-guest"
}

private fun okhttp3.Request.shouldAttachGuestSessionHeader(): Boolean {
    val path = url.encodedPath
    return path.startsWith("/test-sessions/")
}

private fun String?.normalizeJwtToken(): String? =
    this
        ?.trim()
        ?.removePrefix("Bearer ")
        ?.trim()
        ?.takeIf { it.isNotBlank() }