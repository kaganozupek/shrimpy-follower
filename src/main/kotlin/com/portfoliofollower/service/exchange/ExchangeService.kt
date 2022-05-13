package com.portfoliofollower.service.exchange

import com.binance.api.client.domain.market.TickerPrice
import com.portfoliofollower.automator.mapConcurrently
import com.portfoliofollower.repository.portfolio.exchange.ExchangeRepository
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio
import com.portfolioprocessor.model.PortfolioAsset
import kotlinx.coroutines.CoroutineScope
abstract class ExchangeService(
    private val scope: CoroutineScope,
    private val repository: ExchangeRepository
) {

    suspend fun resetPortfolioToUSDT() {
        val allSymbols = repository.getAllSymbols()
        getPortfolio().assets.mapConcurrently {
            runCatching {
                //'^([0-9]{1,20})(\.[0-9]{1,20})?$'
                convertToUsdt(it, it.amount, allSymbols)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    suspend fun getPortfolio(): ExchangePortfolio {
        return repository.getPortfolio()
    }

    private suspend fun convertToBtc(asset: ExchangeAsset, allSymbols: List<TickerPrice>) {
       repository.convertToBtc(asset,0.0, allSymbols)
    }

    private suspend fun convertToUsdt(asset: ExchangeAsset, amount: Double, allSymbols: List<TickerPrice>) {
        repository.convertToUsdt(asset.code, amount, allSymbols)
    }

    suspend fun buySymbol(symbol: String, usdtAmount: Double, allSymbols: List<TickerPrice>) {
        repository.buy(symbol,usdtAmount, allSymbols)
    }

    fun getAllPrices(): List<TickerPrice> {
        return repository.getAllSymbols()
    }


}