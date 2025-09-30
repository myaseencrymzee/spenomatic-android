package com.crymzee.spenomatic.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import com.crymzee.spenomatic.model.ErrorMessage
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

fun SharedPreferences.getDouble(key: String, default: Double?) =
    java.lang.Double.longBitsToDouble(
        getLong(
            key,
            java.lang.Double.doubleToRawLongBits(default ?: 0.0)
        )
    )

fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
    putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun String.isStringEmpty(): Boolean = this.isEmpty() || this.isBlank()

@SuppressLint("Range")
fun ContentResolver.getFileName(uri: Uri): String {
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        name = cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return name
}

fun isValidPhoneNumberOnly(phoneNumber: String): Boolean {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val number = phoneUtil.parse(phoneNumber, null) // Auto-detect country code
        phoneUtil.isValidNumber(number) // ✅ Returns true if valid
    } catch (e: NumberParseException) {
        SpenoMaticLogger.logDebugMsg("PhoneValidation", "Invalid number: ${e.message}")
        false // ❌ Invalid number
    }
}

fun String.toCamelCase(): String =
    lowercase().split(" ", "_").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }


fun parseLocation(location: String): Pair<Double, Double>? {
    // Example: SRID=4326;POINT (31.98909 30.89898)
    val regex = Regex("""POINT\s*\(([-\d.]+)\s+([-\d.]+)\)""")
    val match = regex.find(location)

    return if (match != null) {
        val lon = match.groupValues[1].toDouble()
        val lat = match.groupValues[2].toDouble()
        Pair(lat, lon) // Return as (lat, lng)
    } else {
        null
    }
}


fun extractFirstErrorMessage(error: Throwable?): ErrorMessage {
    return try {
        when (error) {
            is HttpException -> {
                val errorBody = error.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val jsonObject = JSONObject(errorBody)

                    // ✅ Check if key exists
                    val heading = jsonObject.optString("key", "Error")
                        .replaceFirstChar { it.uppercaseChar() } // e.g. validations -> Validations

                    // ✅ Extract first validation message
                    val messagesObj = jsonObject.optJSONObject("messages")
                    val description = messagesObj?.keys()?.asSequence()
                        ?.mapNotNull { field ->
                            messagesObj.optJSONArray(field)?.optString(0)
                                ?: messagesObj.optString(field, null)
                        }
                        ?.firstOrNull() ?: "Something went wrong"

                    ErrorMessage(heading, description)
                } else {
                    ErrorMessage("Error", error.message ?: "Something went wrong")
                }
            }

            is IOException -> ErrorMessage("Network Error", "Please check your connection.")
            else -> ErrorMessage("Error", error?.message ?: "Something went wrong")
        }
    } catch (e: Exception) {
        ErrorMessage("Error", "Something went wrong")
    }
}




fun extractCountryCodeAndNumber(phoneNumber: String): Pair<String, String> {
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
        val trimmed = phoneNumber.trim()

        if (trimmed.startsWith("+")) {
            val number = phoneUtil.parse(trimmed, null)
            val countryCode = number.countryCode.toString() // always numeric
            val nationalNumber = number.nationalNumber.toString()
            Pair(countryCode, nationalNumber)
        } else {
            // no country code provided
            Pair("", trimmed.filter { it.isDigit() })
        }
    } catch (e: Exception) {
        Pair("", phoneNumber.filter { it.isDigit() })
    }
}




fun extractFirstErrorMessagePassword(error: Throwable?): String {
    return try {
        when (error) {
            is HttpException -> {
                val errorBody = error.response()?.errorBody()?.string()
                SpenoMaticLogger.logDebugMsg("ErrorBody", errorBody ?: "No error body") // Debug log

                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        jsonObject.keys().asSequence()
                            .mapNotNull { key -> jsonObject.optJSONArray(key)?.optString(0) }
                            .firstOrNull() ?: "An unknown error occurred"
                    } catch (jsonException: JSONException) {
                        errorBody // Return raw error if JSON parsing fails
                    }
                } else {
                    error.message ?: "An unknown error occurred"
                }
            }

            is IOException -> "Network error. Please check your connection."
            else -> error?.message ?: "An unknown error occurred"
        }
    } catch (e: Exception) {
        "An unknown error occurred"
    }
}