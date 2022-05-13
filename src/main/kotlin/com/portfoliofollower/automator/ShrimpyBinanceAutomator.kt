package com.portfoliofollower.automator

import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.service.exchange.ExchangeService
import com.portfoliofollower.service.exchange.binance.BinanceExchangeService
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.Portfolio
import com.portfoliofollower.service.portfolio.shrimpy.ShrimpyPortfolioService
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ShrimpyBinanceAutomator(
    template: AutomatorInfo,
    scope: CoroutineScope,
    notifier: TelegramNotificationService
): Automator(scope, notifier), KoinComponent {

    override val portfolioService by lazy {
        ShrimpyPortfolioService(scope,template,get(), get()).also {
            it.portfolioChangeListener = this
        }
    }
    override val exchangeService by lazy {
        BinanceExchangeService(
            scope,
            get()
        )
    }
}