package dev.designpattern.adapt.support.error

class CustomException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.message
) : RuntimeException(message)
