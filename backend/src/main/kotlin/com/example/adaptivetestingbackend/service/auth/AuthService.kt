package com.example.adaptivetestingbackend.service.auth

import com.example.adaptivetestingbackend.dto.auth.LoginRequest
import com.example.adaptivetestingbackend.dto.auth.LoginResponse
import com.example.adaptivetestingbackend.dto.auth.MeResponse
import com.example.adaptivetestingbackend.dto.auth.RegisterRequest
import com.example.adaptivetestingbackend.dto.auth.RegisterResponse
import com.example.adaptivetestingbackend.entity.UserEntity
import com.example.adaptivetestingbackend.repository.RoleRepository
import com.example.adaptivetestingbackend.repository.UserRepository
import com.example.adaptivetestingbackend.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        val normalizedEmail = request.email.trim().lowercase()
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists")
        }

        val roleEntity = roleRepository.findByName(request.role)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported role") }

        val user = userRepository.save(
            UserEntity(
                fullName = request.fullName.trim(),
                email = normalizedEmail,
                passwordHash = passwordEncoder.encode(request.password),
                role = roleEntity,
            ),
        )

        return RegisterResponse(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
            role = user.role.name.dbValue,
        )
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): LoginResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        return LoginResponse(token = jwtService.generateToken(user.email))
    }

    @Transactional(readOnly = true)
    fun me(email: String): MeResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found") }

        return MeResponse(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
            role = user.role.name.dbValue,
        )
    }
}
