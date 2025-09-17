package dev.playground.service.rules

import dev.playground.domain.Candle
import dev.playground.domain.Finance
import dev.playground.domain.Indicators

interface SignalRule {
    fun evaluate(ctx: SignalCtx): RuleCheckResult
}

data class SignalCtx(
    val candles: List<Candle>,
) {
    val closes: List<Double>
    val i: Int
    val close: Double

    val s5Series: List<Double?>
    val s20Series: List<Double?>
    val s60Series: List<Double?>
    val rsiSeries: List<Double?>
    val atrSeries: List<Double?>

    val sma5: Double?
    val sma20: Double?
    val sma60: Double?
    val rsi14: Double?
    val atr14: Double?
    val avgTurnover20: Double?
    val prevClose: Double

    init {
        closes = candles.map { it.close }
        i = closes.lastIndex

        close = closes[i]

        s5Series  = closes.indices.map { Indicators.sma(closes, it, 5) }
        s20Series = closes.indices.map { Indicators.sma(closes, it, 20) }
        s60Series = closes.indices.map { Indicators.sma(closes, it, 60) }
        rsiSeries = Indicators.rsiWilder(closes, 14)
        atrSeries = Indicators.atr14(candles)

        sma5  = s5Series[i]
        sma20 = s20Series[i]
        sma60 = s60Series[i]
        rsi14 = rsiSeries[i]
        atr14 = atrSeries[i]

        avgTurnover20 = Finance.avgTurnover20(candles, i)
        prevClose = candles[i - 1].close
    }
}

data class RuleCheckResult(val passed: Boolean, val message: String? = null)
