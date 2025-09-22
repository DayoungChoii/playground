package dev.designpattern.adapt.dto

import com.fasterxml.jackson.annotation.JsonProperty
import dev.designpattern.adapt.common.CarrierType

data class QuoteRequest(
    val carrierType: CarrierType,
    val weightKg: Double,
    val destination: String
)

data class QuoteResponse(
    val provider: CarrierType,
    val totalCost: Long,
    val currency: String,
    val estimatedDays: Int?
)

data class ShipFastRequest(
    val mass: Double,
    val to: String
)

data class ShipFastResponse(
    val rate: Long,
    val currency: String,
    @JsonProperty("eta_days") val etaDays: Int?
)

data class QuickShipResponse(
    val amount: QuickShipAmount,
    val details: QuickShipDetails
)

data class QuickShipAmount(
    val value: Long,
    val unit: String
)

data class QuickShipDetails(
    @JsonProperty("est_days") val estDays: Int?
)