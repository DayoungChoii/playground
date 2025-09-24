package dev.designpattern.adapt.service

import dev.designpattern.adapt.common.CarrierType
import dev.designpattern.adapt.dto.QuoteRequest
import dev.designpattern.adapt.dto.QuoteResponse
import dev.designpattern.adapt.dto.ShipFastRequest
import dev.designpattern.adapt.dto.ShipFastResponse
import dev.designpattern.adapt.support.error.ErrorCode.*
import dev.designpattern.adapt.support.error.ExternalQuoteException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.net.URI

class ShipFastQuoter: ShippingQuoter {
    private val rt = RestTemplate()
    private val shipFastBase = "https://api.shipfast.example.com"
    override fun quote(request: QuoteRequest): QuoteResponse =
        runCatching {
            val url = URI.create("$shipFastBase/rate")
            val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
            val http = HttpEntity(
                ShipFastRequest(mass = request.weightKg, to = request.destination),
                headers
            )

            val resp = rt.postForEntity(url, http, ShipFastResponse::class.java).body
                ?: throw ExternalQuoteException(UPSTREAM_INVALID_RESPONSE)

            QuoteResponse(
                provider = CarrierType.SHIP_FAST,
                totalCost = resp.rate,
                currency = resp.currency,
                estimatedDays = resp.etaDays
            )
        }.getOrElse {
            throw ExternalQuoteException(UPSTREAM_INVALID_RESPONSE)
        }
}