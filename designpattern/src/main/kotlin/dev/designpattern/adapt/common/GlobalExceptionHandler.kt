package dev.designpattern.adapt.common

import dev.designpattern.adapt.support.error.CustomException
import dev.designpattern.adapt.support.error.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(e.errorCode.code, e.message ?: "Unknown error")
        return ResponseEntity.status(e.errorCode.httpStatus).body(response)
    }
}
