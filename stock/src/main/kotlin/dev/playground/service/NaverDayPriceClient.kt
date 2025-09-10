package dev.playground.service

import dev.playground.domain.Candle
import dev.playground.domain.DateFmt
import dev.playground.domain.Num
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NaverDayPriceClient {
    /**
     * 종목 일봉: https://finance.naver.com/item/sise_day.naver?code={6자리}&page={n}
     * 표 컬럼: 날짜, 종가, 전일비, 시가, 고가, 저가, 거래량
     */
    fun fetchStockDaily(code: String, pages: Int = 6): List<Candle> {
        require(code.matches(Regex("""\d{6}"""))) { "code must be 6 digits" }
        require(pages in 1..20)

        val all = mutableListOf<Candle>()
        for (p in 1..pages) {
            val url = "https://finance.naver.com/item/sise_day.naver?code=$code&page=$p"
            val doc = load(url)
            all += parseTable(doc)
            Thread.sleep(200L)
        }
        return all.distinctBy { it.date }.sortedBy { it.date }
    }

    private fun load(url: String): Document =
        Jsoup.connect(url)
            .userAgent("Mozilla/5.0")
            .timeout(5000)
            .get()

    private fun parseTable(doc: Document): List<Candle> {
        val rows = doc.select("table.type2 tr")
        val out = mutableListOf<Candle>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size < 7) continue

            val dateText = tds[0].text().trim()
            val closeText = tds[1].text().trim()
            val openText  = tds[3].text().trim()
            val highText  = tds[4].text().trim()
            val lowText   = tds[5].text().trim()
            val volText   = tds[6].text().trim()

            if (!dateText.matches(Regex("""\d{4}\.\d{2}\.\d{2}"""))) continue

            val date = LocalDate.parse(dateText, DateFmt.NAVER)
            val close = Num.parseDouble(closeText) ?: continue
            val open  = Num.parseDouble(openText) ?: continue
            val high  = Num.parseDouble(highText) ?: continue
            val low   = Num.parseDouble(lowText) ?: continue
            val vol   = Num.parseLong(volText) ?: continue

            out += Candle(date, open, high, low, close, vol)
        }
        return out
    }
}