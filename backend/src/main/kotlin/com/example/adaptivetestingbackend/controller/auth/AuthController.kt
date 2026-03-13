package com.example.adaptivetestingbackend.controller.auth

import com.example.adaptivetestingbackend.dto.auth.LoginRequest
import com.example.adaptivetestingbackend.dto.auth.LoginResponse
import com.example.adaptivetestingbackend.dto.auth.MeResponse
import com.example.adaptivetestingbackend.dto.auth.RegisterRequest
import com.example.adaptivetestingbackend.dto.auth.RegisterResponse
import com.example.adaptivetestingbackend.service.auth.AuthService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): RegisterResponse = authService.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse = authService.login(request)

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal userDetails: UserDetails): MeResponse = authService.me(userDetails.username)
}
