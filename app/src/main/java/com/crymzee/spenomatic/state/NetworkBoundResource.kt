package com.crymzee.spenomatic.state

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.HttpException
import java.io.IOException

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    val data = query().firstOrNull()

    val flow = if (shouldFetch(data ?: return@flow)) {
        emit(Resource.Loading(data))
        try {
            val response = fetch()
            saveFetchResult(response)
            query().map { Resource.Success(it, 200) } // Assuming success is 200
        } catch (throwable: Throwable) {
            flowOf(handleThrowable(throwable, data))
        }
    } else {
        query().map { Resource.Success(it, 200) }
    }

    emitAll(flow)
}.flowOn(Dispatchers.IO)

inline fun <T> networkBoundResource(
    crossinline fetch: suspend () -> T
) = channelFlow {
    send(Resource.Loading(null))
    try {
        val response = fetch()
        send(Resource.Success(response, 200)) // Assuming success is 200
    } catch (throwable: Throwable) {
        send(handleThrowable(throwable, null))
    }
}.flowOn(Dispatchers.IO)

fun <T> handleThrowable(throwable: Throwable, data: T?): Resource<T> {
    return when (throwable) {
        is HttpException -> {
            val errorCode = throwable.code()
            val errorMessage = throwable.message()

            when (errorCode) {
                400 -> Resource.Error(throwable, data, "Bad Request (400): $errorMessage")
                401 -> Resource.Error(throwable, data, "Unauthorized (401): Invalid credentials")
                403 -> Resource.Error(throwable, data, "Forbidden (403): Access denied")
                404 -> Resource.Error(throwable, data, "Not Found (404): Resource missing")
                500 -> Resource.Error(throwable, data, "Server Error (500): Internal server issue")
                else -> Resource.Error(throwable, data, "HTTP Error ($errorCode): $errorMessage")
            }
        }

        is IOException -> Resource.Error(throwable, data, "Network Error: Please check your internet connection.")
        else -> Resource.Error(throwable, data, "Unexpected Error: ${throwable.localizedMessage}")
    }
}
