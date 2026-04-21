package com.example.adaptivetestingbackend.controller.token

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/controller/test-management")
class ControllerTestManagementController {
    @GetMapping("/categories")
    fun categories(): Map<String, String> = mapOf("debug" to "TOKENS_CONTROLLER_IS_LIVE")

    @GetMapping("/tokens")
    fun myTokens(): Map<String, String> = mapOf("debug" to "TOKENS_GET_IS_LIVE")

    @GetMapping("/ping")
    fun ping(): Map<String, String> = mapOf("debug" to "PING_IS_LIVE")
}