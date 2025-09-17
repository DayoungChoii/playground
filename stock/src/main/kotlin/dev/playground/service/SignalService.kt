package dev.playground.service

import dev.playground.domain.*
import dev.playground.service.rules.SignalCtx
import dev.playground.service.rules.SignalRule
import org.springframework.stereotype.Service

@Service
class SignalService(
    private val stockClient: NaverDayPriceClient,
    private val signalRules: List<SignalRule>
) {

    /**
     * 보수 규칙 (시장 필터 제거 버전)
     * - 종목 추세: SMA5 > SMA20 > SMA60 & SMA20 기울기 ≥ 0
     * - RSI: 50~60 or 최근 3일 내 50선 상향 돌파, 과열 RSI ≤ 70
     * - 유동성: 20일 평균 거래대금 ≥ 50억
     * - 변동성: ATR14/Close ≤ 5%
     * - 갭 추격 방지: |전일 대비| ≤ 7%
     */
    fun decideConservative(code: String, stockPages: Int = 6): SignalResponse {
        val candles = stockClient.fetchStockDaily(code, stockPages)
        require(candles.size >= 60) { "not enough stock data (need >=60 rows)" }

        val ctx = SignalCtx(candles)
        val messages = signalRules.failMessages(ctx)
        val isBuy = messages.isEmpty()

        return SignalResponse(
            decision = if (isBuy) Decision.BUY else Decision.HOLD,
            messages = if (isBuy) listOf("모든 보수 조건 충족") else messages
        )
    }
}

fun List<SignalRule>.failMessages(ctx: SignalCtx): List<String> =
    mapNotNull { rule -> rule.evaluate(ctx).let { if (it.passed) null else it.message?.let { msg -> msg } } }

