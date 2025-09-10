package dev.playground.service

import dev.playground.domain.Candle
import dev.playground.domain.DateFmt
import dev.playground.domain.Num
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NaverIndexDayPriceClient {
    /**
     * 지수 일봉: https://finance.naver.com/sise/sise_index_day.naver?code=KOSPI&page={n}
     * code: KOSPI | KOSDAQ | KPI200
     * 표 컬럼: 날짜, 종가, 전일비, 시가, 고가, 저가, 거래량
     */
    fun fetchIndexDaily(code: String, pages: Int = 15): List<Candle> {
        require(code in setOf("KOSPI", "KOSDAQ", "KPI200"))
        require(pages in 1..20)

        val all = mutableListOf<Candle>()
        for (p in 1..pages) {
            val url = "https://finance.naver.com/sise/sise_index_day.naver?code=$code&page=$p"
            val doc = load(url)
            all += parseTable(doc)
            Thread.sleep(200L)
        }
        return all.distinctBy { it.date }.sortedBy { it.date }
    }

    fun fetchIndexDailyEnough(
        code: String,
        minRequired: Int = 230,   // SMA200 계산 + 버퍼
        maxPages: Int = 20
    ): List<Candle> {
        require(code in setOf("KOSPI", "KOSDAQ", "KPI200"))
        val all = mutableListOf<Candle>()
        var page = 1
        while (page <= maxPages) {
            val url = "https://finance.naver.com/sise/sise_index_day.naver?code=$code&page=$page"
            val doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get()
            all += parseTable(doc)
            val uniq = all.distinctBy { it.date }.sortedBy { it.date }
            if (uniq.size >= minRequired) return uniq
            Thread.sleep(200L)
            page++
        }
        return all.distinctBy { it.date }.sortedBy { it.date }
    }

    private fun load(url: String): Document =
        Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(5000)
            .get()

    private fun parseTable(doc: Document): List<Candle> {
        val rows = doc.select("table.type_1 tr, table tr") // 페이지 구조 변화 대응
        val out = mutableListOf<Candle>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size < 6) continue

            val dateText = tds[0].text().trim()
            val closeText = tds[1].text().trim()
            val openText  = tds.getOrNull(3)?.text()?.trim() ?: ""
            val highText  = tds.getOrNull(4)?.text()?.trim() ?: ""
            val lowText   = tds.getOrNull(5)?.text()?.trim() ?: ""
            val volText   = tds.getOrNull(6)?.text()?.trim() ?: "0"

            if (!dateText.matches(Regex("""\d{4}\.\d{2}\.\d{2}"""))) continue

            val date = LocalDate.parse(dateText, DateFmt.NAVER)
            val close = Num.parseDouble(closeText) ?: continue
            val open  = Num.parseDouble(openText) ?: close
            val high  = Num.parseDouble(highText) ?: close
            val low   = Num.parseDouble(lowText) ?: close
            val vol   = Num.parseLong(volText) ?: 0L

            out += Candle(date, open, high, low, close, vol)
        }
        return out
    }
}