package com.example.adaptivetestingbackend.config

import com.example.adaptivetestingbackend.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/health",
                    "/actuator/health",
                    "/auth/register",
                    "/auth/login",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                ).permitAll()

                    .requestMatchers("/token-access/preview", "/token-access/start-guest").permitAll()
                    .requestMatchers("/token-access/start-candidate").hasRole("CANDIDATE")
                    .requestMatchers("/me/results", "/me/results/**").hasRole("CANDIDATE")

                    // ВАЖНО: новый путь token-management
                    .requestMatchers("/token-management/**").hasRole("CONTROLLER")

                    .requestMatchers("/controller/**", "/api/controller/**").hasRole("CONTROLLER")
                    .requestMatchers("/custom-tests/available", "/custom-tests/*/submissions").authenticated()
                    .requestMatchers("/custom-tests/**").hasRole("CONTROLLER")
                    .requestMatchers("/test-sessions/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}