package dev.playground.service

import dev.playground.domain.*
import org.springframework.stereotype.Service
import java.lang.Math.abs

@Service
class SignalService(
    private val stockClient: NaverDayPriceClient
) {
    // ===== Conservative Presets (시장 필터 제거) =====
    private val RSI_MAX = 70.0                  // 과열 상한
    private val VOLATILITY_MAX = 0.05           // 변동성 가드 (ATR/Close ≤ 5%)
    private val GAP_LIMIT = 0.07                // 갭 추격 금지 (|Δ| ≤ 7%)
    private val LIQ_MIN = 5_000_000_000.0       // 20일 평균 거래대금 ≥ 50억

    /**
     * 보수 규칙 (시장 필터 제거 버전)
     * - 종목 추세: SMA5 > SMA20 > SMA60 & SMA20 기울기 ≥ 0
     * - RSI: 50~60 or 최근 3일 내 50선 상향 돌파, 과열 RSI ≤ 70
     * - 유동성: 20일 평균 거래대금 ≥ 50억
     * - 변동성: ATR14/Close ≤ 5%
     * - 갭 추격 방지: |전일 대비| ≤ 7%
     */
    fun decideConservative(
        code: String,
        stockPages: Int = 6
    ): SignalResponse {
        // 1) 종목 데이터 수급
        val candles = stockClient.fetchStockDaily(code, stockPages)
        require(candles.size >= 60) { "not enough stock data (need >=60 rows)" }

        // ===== 종목 지표 계산 =====
        val closes = candles.map { it.close }
        val i = closes.lastIndex

        val s5Series  = closes.indices.map { Indicators.sma(closes, it, 5) }
        val s20Series = closes.indices.map { Indicators.sma(closes, it, 20) }
        val s60Series = closes.indices.map { Indicators.sma(closes, it, 60) }
        val rsiSeries = Indicators.rsiWilder(closes, 14)
        val atrSeries = Indicators.atr14(candles)

        val sma5  = s5Series[i]
        val sma20 = s20Series[i]
        val sma60 = s60Series[i]
        val rsi14 = rsiSeries[i]
        val atr14 = atrSeries[i]

        val prevClose = candles[i - 1].close
        val close = closes[i]

        // ===== 규칙 계산 =====
        val reasons = mutableListOf<String>()

        // (1) 종목 추세 체인 + 기울기
        val trendOk = (sma5 != null && sma20 != null && sma60 != null &&
                sma5 > sma20 && sma20 > sma60 &&
                (Indicators.slope(s20Series, i, 3) ?: Double.NEGATIVE_INFINITY) >= 0.0)
        if (!trendOk) reasons += "추세 체인/기울기 불만족"

        // (2) RSI 필터
        val rsiOk = (rsi14 != null && rsi14 in 50.0..60.0) ||
                Indicators.crossedUp(rsiSeries, i, threshold = 50.0, lookbackDays = 3)
        if (!rsiOk) reasons += "RSI 조건 불만족"
        val notOverbought = (rsi14 == null || rsi14 <= RSI_MAX)
        if (!notOverbought) reasons += "과열(RSI>$RSI_MAX)"

        // (3) 유동성 가드: 20일 평균 거래대금
        val avgTurnover20 = Finance.avgTurnover20(candles, i)
        val liquidityOk = (avgTurnover20 != null && avgTurnover20 >= LIQ_MIN)
        if (!liquidityOk) reasons += "거래대금 부족(<50억)"

        // (4) 변동성 가드: ATR14/Close ≤ 5%
        val volatilityOk = (atr14 != null && atr14 / close <= VOLATILITY_MAX)
        if (!volatilityOk) reasons += "변동성 과다(ATR/Close>${(VOLATILITY_MAX*100).toInt()}%)"

        // (5) 갭 추격 방지: |전일 대비| ≤ 7%
        val gapOk = abs(Finance.pctChange(prevClose, close)) <= GAP_LIMIT
        if (!gapOk) reasons += "갭 급등/급락(> ${(GAP_LIMIT*100).toInt()}%)"

        val buy = trendOk && rsiOk && notOverbought && liquidityOk && volatilityOk && gapOk
        if (buy) {
            reasons.clear()
            reasons += "모든 보수 조건 충족"
        }

        val last = candles.last()
        return SignalResponse(
            code = code,
            date = last.date.format(DateFmt.ISO),
            close = Indicators.round2(close),
            sma5 = sma5?.let(Indicators::round2),
            sma20 = sma20?.let(Indicators::round2),
            sma60 = sma60?.let(Indicators::round2),
            rsi14 = rsi14?.let(Indicators::round2),
            kospiSma50 = null,          // 시장 필터 제거 → null로 반환
            kospiSma200 = null,         // 시장 필터 제거 → null로 반환
            decision = if (buy) Decision.BUY else Decision.HOLD,
            reasons = reasons
        )
    }
}
