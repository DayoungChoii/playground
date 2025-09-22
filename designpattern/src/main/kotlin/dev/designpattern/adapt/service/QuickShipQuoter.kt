package dev.designpattern.adapt.service

import dev.designpattern.adapt.dto.QuoteRequest
import dev.designpattern.adapt.dto.QuoteResponse

class QuickShipQuoter: ShippingQuoter {
    override fun quote(request: QuoteRequest): QuoteResponse {
        TODO("Not yet implemented")
    }
}