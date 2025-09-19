package dev.designpattern.adapt.support.error

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*

enum class ErrorCode(
    val code: String,
    val message: String,
    val httpStatus: HttpStatus
) {
    UPSTREAM_INVALID_RESPONSE("UPSTREAM_INVALID_RESPONSE", "외부 응답 포맷이 올바르지 않습니다.", BAD_GATEWAY),
}
