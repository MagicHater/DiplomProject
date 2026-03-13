package com.example.adaptivetestingbackend.entity

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

enum class RoleName(@get:JsonValue val dbValue: String) {
    CANDIDATE("candidate"),
    CONTROLLER("controller");

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromDbValue(value: String): RoleName = entries.firstOrNull { it.dbValue == value }
            ?: throw IllegalArgumentException("Unknown role name: $value")
    }
}

@Converter
class RoleNameConverter : AttributeConverter<RoleName, String> {
    override fun convertToDatabaseColumn(attribute: RoleName): String = attribute.dbValue

    override fun convertToEntityAttribute(dbData: String): RoleName = RoleName.fromDbValue(dbData)
}
