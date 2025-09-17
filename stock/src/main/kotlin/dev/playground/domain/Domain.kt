package dev.playground.domain

import java.lang.Math.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Candle(
    val date: LocalDate,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

enum class Decision { BUY, HOLD }

data class SignalResponse(
    val decision: Decision,
    val messages: List<String>
)

object DateFmt {
    val NAVER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
}

object Num {
    fun parseDouble(txt: String): Double? =
        txt.trim().replace(",", "").takeIf { it.matches(Regex("-?\\d+(\\.\\d+)?")) }?.toDouble()

    fun parseLong(txt: String): Long? =
        txt.trim().replace(",", "").takeIf { it.matches(Regex("-?\\d+")) }?.toLong()
}

object Indicators {
    fun sma(series: List<Double>, idx: Int, window: Int): Double? {
        if (idx + 1 < window) return null
        val s = series.subList(idx - window + 1, idx + 1)
        return s.average()
    }

    /** Wilder RSI(14) */
    fun rsiWilder(closes: List<Double>, period: Int = 14): List<Double?> {
        if (closes.size < period + 1) return List(closes.size) { null }

        val gains = MutableList(closes.size) { 0.0 }
        val losses = MutableList(closes.size) { 0.0 }
        for (i in 1 until closes.size) {
            val diff = closes[i] - closes[i - 1]
            if (diff > 0) gains[i] = diff else losses[i] = -diff
        }
        val out = MutableList<Double?>(closes.size) { null }
        var avgGain = gains.subList(1, period + 1).sum() / period
        var avgLoss = losses.subList(1, period + 1).sum() / period

        fun toRsi(g: Double, l: Double): Double {
            return when {
                l == 0.0 && g == 0.0 -> 50.0
                l == 0.0 -> 100.0
                g == 0.0 -> 0.0
                else -> {
                    val rs = g / l
                    100.0 - (100.0 / (1 + rs))
                }
            }
        }

        out[period] = toRsi(avgGain, avgLoss)
        for (i in (period + 1) until closes.size) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period
            out[i] = toRsi(avgGain, avgLoss)
        }
        return out
    }

    /** ATR14 (간단: 최근 14개 TR의 산술 평균) */
    fun atr14(candles: List<Candle>): List<Double?> {
        if (candles.size < 15) return List(candles.size) { null }
        val tr = MutableList(candles.size) { 0.0 }
        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val pc = candles[i - 1].close
            val trVal = max(h - l, max(abs(h - pc), abs(l - pc)))
            tr[i] = trVal
        }
        val out = MutableList<Double?>(candles.size) { null }
        for (i in 14 until candles.size) {
            val slice = tr.subList(i - 13, i + 1) // 14개
            out[i] = slice.average()
        }
        return out
    }

    /** 최근 lookback일 기준 단순 기울기: value(i) - value(i-lookback) */
    fun slope(series: List<Double?>, idx: Int, lookback: Int): Double? {
        if (idx - lookback < 0) return null
        val cur = series[idx] ?: return null
        val prev = series[idx - lookback] ?: return null
        return cur - prev
    }

    /** 최근 N일 내 임계선 상향 돌파(연속 날짜 기준) */
    fun crossedUp(series: List<Double?>, idx: Int, threshold: Double, lookbackDays: Int): Boolean {
        var checked = 0
        var k = idx
        while (k > 0 && checked < lookbackDays) {
            val prev = series[k - 1]
            val cur = series[k]
            if (prev != null && cur != null && prev < threshold && cur >= threshold) return true
            checked++
            k--
        }
        return false
    }

    fun round2(v: Double): Double = round(v * 100.0) / 100.0
}

object Finance {
    /** 20일 평균 거래대금(원) */
    fun avgTurnover20(candles: List<Candle>, idx: Int): Double? {
        if (idx + 1 < 20) return null
        val s = candles.subList(idx - 19, idx + 1)
        val vals = s.map { it.close * it.volume }
        return vals.average()
    }

    /** 전일 대비 등락률 */
    fun pctChange(prevClose: Double, close: Double): Double =
        (close - prevClose) / prevClose
}