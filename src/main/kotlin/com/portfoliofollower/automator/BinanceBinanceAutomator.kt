package com.portfoliofollower.automator

import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.service.abstract.PortfolioService
import com.portfoliofollower.service.exchange.ExchangeService
import com.portfoliofollower.service.exchange.binance.BinanceExchangeService
import com.portfoliofollower.service.notification.DiscordNotificationService
import com.portfoliofollower.service.notification.TelegramNotificationService
import kotlinx.coroutines.CoroutineScope

class BinanceBinanceAutomator(
    scope: CoroutineScope,
    notifier: TelegramNotificationService,
    private val _PortfolioService: PortfolioService,
    private val _ExchangeService: ExchangeService,
    private val discordNotificationService: DiscordNotificationService

): Automator(scope, notifier,discordNotificationService) {
    override val portfolioService: PortfolioService
        get() = _PortfolioService.also {
            it.portfolioChangeListener = this
        }
    override val exchangeService: ExchangeService
        get() = _ExchangeService
}