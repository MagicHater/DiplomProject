package com.example.adaptivetestingbackend.service

import com.example.adaptivetestingbackend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun existsByEmail(email: String): Boolean {
        return userRepository.findByEmail(email).isPresent
    }
}
