package dev.playground.service.rules

import dev.playground.domain.Finance

class GapChaseGuardRule: SignalRule {
    override fun evaluate(ctx: SignalCtx): RuleCheckResult {
        val gapOk = Math.abs(Finance.pctChange(ctx.prevClose, ctx.close)) <= GAP_LIMIT

        if(gapOk) return RuleCheckResult(passed = true)
        return RuleCheckResult(passed = false, message = "갭 급등/급락(> ${(GAP_LIMIT*100).toInt()}%)")
    }
}