package dev.designpattern.adapt.service

import dev.designpattern.adapt.common.CarrierType
import dev.designpattern.adapt.dto.*
import dev.designpattern.adapt.support.error.ErrorCode
import dev.designpattern.adapt.support.error.ExternalQuoteException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

class QuickShipQuoter: ShippingQuoter {
    private val rt = RestTemplate()
    private val quickShipBase = "https://api.quickship.example.com"
    override fun quote(request: QuoteRequest): QuoteResponse =
        runCatching {
            val grams = (request.weightKg * 1000).toLong()
            val url = UriComponentsBuilder
                .fromHttpUrl("$quickShipBase/quote")
                .queryParam("weight", grams)
                .queryParam("dest", request.destination)
                .build()
                .toUri()

            val resp = rt.getForEntity(url, QuickShipResponse::class.java).body
                ?: throw ExternalQuoteException(ErrorCode.UPSTREAM_INVALID_RESPONSE)

            QuoteResponse(
                provider = CarrierType.SHIP_FAST,
                totalCost = resp.amount.value,
                currency = resp.amount.unit,
                estimatedDays = resp.details.estDays
            )
        }.getOrElse {
            throw ExternalQuoteException(ErrorCode.UPSTREAM_INVALID_RESPONSE)
        }
}