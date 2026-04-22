package com.example.adaptivetestingbackend.controller.ai

import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftRequest
import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftResponse
import com.example.adaptivetestingbackend.dto.ai.InterpretCustomTestResultRequest
import com.example.adaptivetestingbackend.dto.ai.InterpretCustomTestResultResponse
import com.example.adaptivetestingbackend.service.ai.AiCustomTestDraftService
import com.example.adaptivetestingbackend.service.ai.AiCustomTestInterpretationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/controller/ai/custom-tests")
class AiCustomTestsController(
    private val draftService: AiCustomTestDraftService,
    private val interpretationService: AiCustomTestInterpretationService,
) {

    @PostMapping("/draft")
    fun generateDraft(
        @Valid @RequestBody request: GenerateCustomTestDraftRequest,
    ): GenerateCustomTestDraftResponse = draftService.generateDraft(request)

    @PostMapping("/interpret")
    fun interpret(
        @Valid @RequestBody request: InterpretCustomTestResultRequest,
    ): InterpretCustomTestResultResponse = interpretationService.interpret(request)
}
