package io.axoniq.demo.bikerental.config

class ExpectedException(val type: ErrorType, message: String, cause: Throwable? = null)
    : RuntimeException("${type.name.replace('_', ' ').toLowerCase()}: $message", cause)

enum class ErrorType(val code: Int) {
    ALREADY_RENTED(1),
    ALREADY_RETURNED(2);
}