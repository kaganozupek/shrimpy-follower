package com.portfolioprocessor.model

import com.portfoliofollower.api.ShrimpyProfileDTO

data class PortfolioAsset(
    val code: String,
    val percentage: Int
)

data class Portfolio(
    val assets: List<PortfolioAsset>

) {
    fun toMessage(): String {
        return assets.joinToString("\n") {
            "${it.code} -> %${it.percentage}"
        }
    }
}

fun ShrimpyProfileDTO.toPortfolio(): Portfolio {
    return Portfolio(this.assets.filter {
        it.actualPercent * 100 > 1
    }.map {
        PortfolioAsset(it.currency, (it.actualPercent * 100).toInt())
    })
}

data class ExchangeAsset(
    val code: String,
    val amount: Double,
)

data class ExchangePortfolio(
    val assets: List<ExchangeAsset>
)


