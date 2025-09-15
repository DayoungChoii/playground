package dev.playground.service.rules

import dev.playground.domain.Indicators

class TrendChainRule : SignalRule{
    override fun evaluate(ctx: SignalCtx): RuleCheckResult {
        val trendOk = (ctx.sma5 != null && ctx.sma20 != null && ctx.sma60 != null &&
                ctx.sma5 > ctx.sma20 && ctx.sma20 > ctx.sma60 &&
                (Indicators.slope(ctx.s20Series, ctx.i, 3) ?: Double.NEGATIVE_INFINITY) >= 0.0)
        if (!trendOk) return RuleCheckResult(false, "추세 체인/기울기 불만족")
        return RuleCheckResult(true)
    }
}