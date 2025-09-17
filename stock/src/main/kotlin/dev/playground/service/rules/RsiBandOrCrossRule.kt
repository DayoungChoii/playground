package dev.playground.service.rules

import dev.playground.domain.Indicators
import org.springframework.stereotype.Component

@Component
class RsiBandOrCrossRule : SignalRule{
    override fun evaluate(ctx: SignalCtx): RuleCheckResult {
        val rsiOk =
            (ctx.rsi14?.let { it in 50.0..60.0 } == true) ||
                    Indicators.crossedUp(ctx.rsiSeries, ctx.i, threshold = 50.0, lookbackDays = 3)

        val overbought = (ctx.rsi14?.let { it > RSI_MAX } == true)

        if (rsiOk && !overbought) return RuleCheckResult(passed = true)

        val reason = buildList<String> {
            if (!rsiOk) add("RSI 조건 불만족")
            if (overbought) add("과열(RSI>$RSI_MAX)")
        }.joinToString(" · ")

        return RuleCheckResult(passed = false, message = reason)
    }
}