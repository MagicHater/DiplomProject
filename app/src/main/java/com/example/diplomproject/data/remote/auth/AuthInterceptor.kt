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

        if (!originalRequest.shouldSkipAuthHeader()) {
            val token = runBlocking { sessionManager.tokenFlow.firstOrNull() }
            if (!token.isNullOrBlank()) {
                requestBuilder = requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        if (originalRequest.shouldAttachGuestSessionHeader()) {
            val guestSessionKey = runBlocking { sessionManager.guestSessionKeyFlow.firstOrNull() }
            if (!guestSessionKey.isNullOrBlank()) {
                requestBuilder = requestBuilder.addHeader("X-Guest-Session-Key", guestSessionKey)
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}

private fun okhttp3.Request.shouldSkipAuthHeader(): Boolean {
    val path = url.encodedPath
    return path == "/auth/login" || path == "/auth/register"
}

private fun okhttp3.Request.shouldAttachGuestSessionHeader(): Boolean {
    val path = url.encodedPath
    return path.startsWith("/test-sessions/")
}
