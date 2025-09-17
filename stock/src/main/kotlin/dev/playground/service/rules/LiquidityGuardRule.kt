package dev.playground.service.rules

import dev.playground.domain.Finance
import org.springframework.stereotype.Component

@Component
class LiquidityGuardRule: SignalRule {
    override fun evaluate(ctx: SignalCtx): RuleCheckResult {
        val avgTurnover20 = ctx.avgTurnover20
        val liquidityOk = (avgTurnover20 != null && avgTurnover20 >= LIQ_MIN)

        if(liquidityOk) return RuleCheckResult(passed = true)
        return RuleCheckResult(passed = false, message = "거래대금 부족(<50억)")
    }
}