package dev.playground.controller

import dev.playground.domain.SignalResponse
import dev.playground.service.SignalService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class SignalController(
    private val signalService: SignalService
) {
    /**
     * 예:
     *  /signal/conservative?code=042660
     *  /signal/conservative?code=005930&stockPages=6&indexPages=12
     */
    @GetMapping("/signal/conservative")
    fun conservative(
        @RequestParam code: String,
        @RequestParam(required = false, defaultValue = "6") stockPages: Int,
    ): SignalResponse = signalService.decideConservative(code, stockPages)
}