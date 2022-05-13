package com.portfoliofollower.model

import java.util.UUID

data class AutomatorInfo(
    val leaderId: String,
    val portfolioID: String,
    val automatorType: AutomatorType,
    val id: String = UUID.randomUUID().toString(),

)

enum class AutomatorType(
    type: Int
) {
    SHRIMPY_BINANCE(0)
}