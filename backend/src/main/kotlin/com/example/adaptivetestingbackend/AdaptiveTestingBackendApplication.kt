package com.example.adaptivetestingbackend

import com.example.adaptivetestingbackend.config.AiYandexProperties
import com.example.adaptivetestingbackend.config.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class, AiYandexProperties::class)
class AdaptiveTestingBackendApplication

fun main(args: Array<String>) {
    runApplication<AdaptiveTestingBackendApplication>(*args)
}
