package com.crymzee.spenomatic.state


sealed class DataState<out R> {
    data class Success<T>(var data: T) : DataState<T>()
    data class Error(val error: String) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    object NetworkError : DataState<Nothing>()
}
