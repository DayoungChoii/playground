package dev.designpattern.adapt.service

import dev.designpattern.adapt.dto.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SignalService(

) {
    private val rt = RestTemplate()
    private val shipFastBase = "https://api.shipfast.example.com"
    private val quickShipBase = "https://api.quickship.example.com"

    fun estimateQuote(request: QuoteRequest): ResponseEntity<Any> {
        return try {
            when (request.carrier.uppercase()) {
                "SHIPFAST" -> shipFastQuote(request)
                "QUICKSHIP" -> quickShipQuote(request)
                else -> ResponseEntity.badRequest().body(
                    mapOf("error" to "unsupported carrier: ${request.carrier}")
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(502).body(mapOf("error" to (e.message ?: "upstream error")))
        }
    }

    private fun shipFastQuote(req: QuoteRequest): ResponseEntity<Any> {
        val url = URI.create("$shipFastBase/rate")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val body = ShipFastRequest(mass = req.weightKg, to = req.destination)
        val http = HttpEntity(body, headers)

        val resp = rt.postForEntity(url, http, ShipFastResponse::class.java).body
            ?: return ResponseEntity.status(502).body(mapOf("error" to "empty response"))

        val out = QuoteResponse(
            provider = "SHIPFAST",
            totalCost = resp.rate,
            currency = resp.currency,
            estimatedDays = resp.etaDays
        )
        return ResponseEntity.ok(out)
    }

    private fun quickShipQuote(req: QuoteRequest): ResponseEntity<Any> {
        val grams = (req.weightKg * 1000).toLong()
        val url = UriComponentsBuilder
            .fromHttpUrl("$quickShipBase/quote")
            .queryParam("weight", grams)
            .queryParam("dest", req.destination)
            .build()
            .toUri()

        val resp = rt.getForEntity(url, QuickShipResponse::class.java).body
            ?: return ResponseEntity.status(502).body(mapOf("error" to "empty response"))

        val out = QuoteResponse(
            provider = "QUICKSHIP",
            totalCost = resp.amount.value,
            currency = resp.amount.unit,
            estimatedDays = resp.details.estDays
        )
        return ResponseEntity.ok(out)
    }
}
