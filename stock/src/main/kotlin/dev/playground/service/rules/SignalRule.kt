package dev.playground.service.rules

import dev.playground.domain.Candle

interface SignalRule {
    fun evaluate(ctx: SignalCtx): RuleCheckResult
}

data class SignalCtx(
    val candles: List<Candle>,
    val i: Int,
    val s5Series: List<Double?>,
    val s20Series: List<Double?>,
    val s60Series: List<Double?>,
    val rsiSeries: List<Double?>,
    val atrSeries: List<Double?>,
    val sma5: Double?,
    val sma20: Double?,
    val sma60: Double?,
    val rsi14: Double?,
    val atr14: Double?,
    val avgTurnover20: Double?,
) {
    val closes = candles.map { it.close }
    val close = closes[i]
    val prevClose = candles[i - 1].close
}

data class RuleCheckResult(val passed: Boolean, val message: String? = null)
