package com.portfoliofollower.service.exchange

import com.binance.api.client.domain.market.TickerPrice
import com.portfoliofollower.automator.mapConcurrently
import com.portfoliofollower.repository.portfolio.exchange.ExchangeRepository
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio
import com.portfolioprocessor.model.PortfolioAsset
import kotlinx.coroutines.CoroutineScope
abstract class ExchangeService(
    private val scope: CoroutineScope,
    private val repository: ExchangeRepository,
    private val notificationService: TelegramNotificationService
) {

    suspend fun resetPortfolioToUSDT() {
        val allSymbols = repository.getAllSymbols()
        getPortfolio().assets.mapConcurrently {
            runCatching {
                //'^([0-9]{1,20})(\.[0-9]{1,20})?$'
                if(it.code.contains("LUNA")) {
                    convertToBusd(it,it.amount, allSymbols)
                    val busd = getPortfolio().assets.first { it.code.contains("BUSD")}
                    convertToUsdt(busd,busd.amount, allSymbols)
                } else {
                    convertToUsdt(it, it.amount, allSymbols)
                }

                notificationService.sendMessage("INFO", "PORTFOLIO RESET\n\n ${getPortfolio()}")

            }.onFailure { error ->
                notificationService.sendMessage("ERROR","RESET PORTFOLIO USDT\n\n${it}\n\n${error.message}")
                error.printStackTrace()
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

    private suspend fun convertToBusd(asset: ExchangeAsset, amount: Double, allSymbols: List<TickerPrice>) {
        repository.convertToBusd(asset.code, amount, allSymbols)
    }

    suspend fun buySymbol(symbol: String, usdtAmount: Double, allSymbols: List<TickerPrice>) {
        repository.buy(symbol,usdtAmount, allSymbols)
    }

    suspend fun buyPair(symbol: String, usdtAmount: Double, allSymbols: List<TickerPrice>) {
        repository.buyWithPair(symbol,usdtAmount, allSymbols)
    }

    fun getAllPrices(): List<TickerPrice> {
        return repository.getAllSymbols()
    }


}