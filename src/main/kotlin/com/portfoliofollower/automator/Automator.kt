package com.portfoliofollower.automator

import com.binance.api.client.domain.market.TickerPrice
import com.portfolioprocessor.model.Portfolio
import com.portfoliofollower.service.abstract.OnPortfolioChangedListener
import com.portfoliofollower.service.abstract.PortfolioService
import com.portfoliofollower.service.exchange.ExchangeService
import com.portfoliofollower.service.notification.DiscordNotificationService
import com.portfoliofollower.service.notification.TelegramNotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.*

abstract class Automator(private val scope: CoroutineScope, private val notificationService: TelegramNotificationService, private val discordNotificationService: DiscordNotificationService): OnPortfolioChangedListener {

    abstract val portfolioService: PortfolioService
    abstract val exchangeService: ExchangeService


    override suspend fun onPortfolioChanged(templateId: String, portfolio: Portfolio, silent: Boolean) {
        println("LEADER PORTFOLIO CHANGED $portfolio ${Date()}")
        processPortfolioChange(portfolio)
        notificationService.notifyPortfolioChange(portfolio)
        if(!silent) {
            discordNotificationService.notifyPortfolioChange(portfolio)
        }
        println("PROCESS PORTFOLIO FINISHED $portfolio ${Date()}")

    }

    suspend fun processPortfolioChange(portfolio: Portfolio) {
        resetPortfolioToUSDT()
        val usdt = exchangeService.getPortfolio().assets.firstOrNull() { it.code == "USDT" } ?: return
        val allSymbols = exchangeService.getAllPrices()
        portfolio.assets.mapConcurrently {
            if(it.code.uppercase() == "LUNA") {
                buySymbol("BUSD", usdt.amount * it.percentage.toDouble() / 100, allSymbols)
                buyPair("LUNABUSD", usdt.amount * 0.97f * it.percentage.toDouble() / 100, allSymbols)
            } else {
                buySymbol(it.code, usdt.amount * it.percentage.toDouble() / 100, allSymbols)

            }
        }

        notificationService.sendMessage("PORTFOLIO PROCESS FINISHED", "$portfolio \n\n ${exchangeService.getPortfolio()}")


    }

    fun startAutomation() {
        portfolioService.startObservation()
    }


    private suspend fun resetPortfolioToUSDT() {
        exchangeService.resetPortfolioToUSDT()
    }

    suspend fun buySymbol(symbol: String, usdt: Double, allSymbols: List<TickerPrice>) {
       runCatching {
           exchangeService.buySymbol(symbol, usdt, allSymbols)
           notificationService.sendMessage("INFO BUY", "${symbol} SUCCESS" )

       }.onFailure {error ->
           notificationService.sendMessage("ERROR","BUY SYMBOL\n\n${symbol} ${usdt}\n\n${error.message}")
           error.printStackTrace()
       }
    }

    suspend fun buyPair(symbol: String, usdt: Double, allSymbols: List<TickerPrice>) {
        runCatching {
            exchangeService.buyPair(symbol, usdt, allSymbols)
            notificationService.sendMessage("INFO BUY", "${symbol} SUCCESS" )
        }.onFailure { error ->
            notificationService.sendMessage("ERROR","BUY SYMBOL\n\n${symbol} ${usdt}\n\n${error.message}")
            error.printStackTrace()
        }
    }
}


suspend fun <TInput, TOutput> Iterable<TInput>.mapConcurrently(
    maxConcurrency: Int = 5,
    transform: suspend (TInput) -> TOutput,
) = coroutineScope {
    val gate = Semaphore(maxConcurrency)
    this@mapConcurrently.map {
        async {
            gate.withPermit {
                transform(it)
            }
        }
    }.awaitAll()
}
