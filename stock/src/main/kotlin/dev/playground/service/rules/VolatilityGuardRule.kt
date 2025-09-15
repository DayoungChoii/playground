package dev.playground.service.rules

class VolatilityGuardRule: SignalRule {
    override fun evaluate(ctx: SignalCtx): RuleCheckResult {
        val volatilityOk = (ctx.atr14 != null && ctx.atr14 / ctx.close <= VOLATILITY_MAX)

        if(volatilityOk) return RuleCheckResult(passed = true)
        return RuleCheckResult(passed = false, message = "변동성 과다(ATR/Close>${(VOLATILITY_MAX*100).toInt()}%)")
    }
}