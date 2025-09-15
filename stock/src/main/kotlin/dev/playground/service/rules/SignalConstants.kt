package dev.playground.service.rules

const val RSI_MAX = 70.0                  // 과열 상한
const val VOLATILITY_MAX = 0.05           // 변동성 가드 (ATR/Close ≤ 5%)
const val GAP_LIMIT = 0.07                // 갭 추격 금지 (|Δ| ≤ 7%)
const val LIQ_MIN = 5_000_000_000.0       // 20일 평균 거래대금 ≥ 50억