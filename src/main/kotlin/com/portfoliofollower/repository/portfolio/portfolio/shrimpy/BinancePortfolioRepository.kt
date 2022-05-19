package com.portfoliofollower.repository.portfolio.portfolio.shrimpy

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.TickerPrice
import com.portfoliofollower.repository.portfolio.portfolio.PortfolioRepository
import com.portfolioprocessor.model.Portfolio
import com.portfolioprocessor.model.PortfolioAsset

class BinancePortfolioRepository(private val client: BinanceApiRestClient): PortfolioRepository {
    override suspend fun getRepository(userId: String, portfolioName: String): Portfolio {

        val balances = client.account.balances.filter { it.free.toDouble() + it.locked.toDouble() > 0.0 }
        val prices = client.allPrices
        val totalAmount = balances.sumOf { ab ->
            return@sumOf when(ab.asset) {
                "USDT" -> {
                    ab.free.toDouble() + ab.locked.toDouble()
                }
                else -> {
                    getAssetUSDPrice(prices,ab.asset, (ab.free.toDouble() + ab.locked.toDouble()))
                }
            }
        }
        val portfolio = Portfolio(balances.map {
            PortfolioAsset(
                it.asset,
                (getAssetUSDPrice(prices, it.asset, (it.free.toDouble() + it.locked.toDouble())) /totalAmount * 100).toInt()
            )
        })

        return Portfolio(portfolio.assets.filter { it.percentage > 0 })
    }

    private fun getAssetUSDPrice(prices: List<TickerPrice>, symbol: String, amount: Double): Double {
        if(symbol == "USDT" || symbol == "BUSD") {
            return amount
        }
        val price = prices.firstOrNull { it.symbol == "${symbol.toUpperCase()}USDT" }?.price?.toDouble() ?: prices.firstOrNull { it.symbol == "${symbol}BUSD" }?.price?.toDouble() ?: 0.0
        return amount * price
    }
}