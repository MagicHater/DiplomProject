package com.example.adaptivetestingbackend.service.ai.prompt

import com.example.adaptivetestingbackend.dto.ai.GenerateCustomTestDraftRequest
import org.springframework.stereotype.Component

@Component
class CustomTestDraftPromptBuilder {

    fun build(request: GenerateCustomTestDraftRequest): String {
        val audienceLine = request.audience?.let { "Audience: $it" } ?: "Audience: not specified"
        val questionsLine = request.desiredQuestionCount?.let { "Desired question count: $it" }
            ?: "Desired question count: choose optimal amount"
        val languageLine = request.language?.let { "Language: $it" } ?: "Language: same as prompt"

        return """
    Ты — специалист по разработке психологических и поведенческих тестов для оценки кандидатов.
    Сгенерируй пользовательский тест на основе запроса пользователя.

    ВАЖНО:
    - Ответ должен быть ТОЛЬКО валидным JSON.
    - Не используй markdown.
    - Не используй блоки ```json.
    - Не добавляй пояснения до или после JSON.
    - Все тексты должны быть на русском языке, если язык указан как ru.
    - Не используй шаблонные фразы вроде "Название теста", "Описание теста", "Текст вопроса".
    - Вопросы должны быть конкретными и соответствовать теме запроса.
    - Варианты ответов должны быть осмысленными и различаться по поведению кандидата.
    - Каждый вопрос должен иметь от 3 до 4 вариантов ответа.

    Тема и цель теста:
    ${request.prompt.trim()}

    Ограничения:
    - $audienceLine
    - $questionsLine
    - $languageLine

    Верни JSON строго в следующем формате:
    {
      "title": "Краткое название теста",
      "description": "Краткое описание того, что оценивает тест",
      "questions": [
        {
          "text": "Конкретный вопрос или рабочая ситуация",
          "options": [
            "Первый вариант поведения",
            "Второй вариант поведения",
            "Третий вариант поведения"
          ]
        }
      ]
    }

    Пример качества:
    Если тема — стрессоустойчивость, вопросы должны описывать ситуации давления, срочных задач,
    конфликтов, высокой нагрузки или необходимости быстро сохранять концентрацию.
        Запрещено использовать следующие значения:
        - "Название теста"
        - "Описание теста"
        - "Текст вопроса"
        - "Вариант 1"
        - "Вариант 2"
        - "string"

        Если пользовательский запрос слишком общий, всё равно сгенерируй содержательный тест по указанной теме.
""".trimIndent()
    }
}
