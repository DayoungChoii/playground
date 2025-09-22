package dev.designpattern.adapt.service

import dev.designpattern.adapt.dto.QuoteRequest
import dev.designpattern.adapt.dto.QuoteResponse

interface ShippingQuoter {
    fun quote(request: QuoteRequest): QuoteResponse
}