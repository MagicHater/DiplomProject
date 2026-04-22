package com.example.adaptivetestingbackend.ai.exception

open class AiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class AiDisabledException(message: String) : AiException(message)

class AiClientNotConfiguredException(message: String) : AiException(message)

class AiProviderUnavailableException(message: String, cause: Throwable? = null) : AiException(message, cause)

class AiResponseParsingException(message: String, cause: Throwable? = null) : AiException(message, cause)
