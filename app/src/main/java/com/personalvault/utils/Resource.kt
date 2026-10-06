package com.personalvault.utils

/**
 * A generic class that holds a value with its loading status.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
