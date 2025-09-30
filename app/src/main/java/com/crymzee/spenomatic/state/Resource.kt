package com.crymzee.spenomatic.state


sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val statusCode: Int? = null,
    val throwable: Throwable? = null
) {
    class Success<T>(data: T?, statusCode: Int? = 200) : Resource<T>(data, statusCode = statusCode)
    class Error<T>(
        throwable: Throwable,
        data: T? = null,
        message: String? = throwable.localizedMessage,
        statusCode: Int? = null
    ) : Resource<T>(data, message, statusCode, throwable)

    class Loading<T>(data: T? = null) : Resource<T>(data)
}
