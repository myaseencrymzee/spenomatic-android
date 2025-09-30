package com.crymzee.spenomatic.enums

import kotlin.text.lowercase

enum class OTPType {
    CREATE,
    FORGOT,
    CHANGE;

    fun toStringValue(): String {
        return name.lowercase()
    }
}