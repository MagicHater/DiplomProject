package com.example.adaptivetestingbackend.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class TestSessionStatus(@get:JsonValue val dbValue: String) {
    CREATED("created"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromDbValue(value: String): TestSessionStatus = entries.firstOrNull { it.dbValue == value }
            ?: throw IllegalArgumentException("Unknown test session status: $value")
    }
}

@Converter
class TestSessionStatusConverter : AttributeConverter<TestSessionStatus, String> {
    override fun convertToDatabaseColumn(attribute: TestSessionStatus): String = attribute.dbValue

    override fun convertToEntityAttribute(dbData: String): TestSessionStatus = TestSessionStatus.fromDbValue(dbData)
}
