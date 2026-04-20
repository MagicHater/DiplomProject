package com.example.adaptivetestingbackend.service.testsession

import com.example.adaptivetestingbackend.entity.UserEntity

sealed interface SessionActor {
    data class CandidateActor(val user: UserEntity) : SessionActor

    data class GuestActor(
        val sessionId: java.util.UUID,
        val guestKey: String,
        val guestName: String? = null,
    ) : SessionActor
}
