package com.portfoliofollower.service.exchange.binance

import com.portfoliofollower.repository.portfolio.exchange.binance.BinanceRepository
import com.portfoliofollower.service.exchange.ExchangeService
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio
import kotlinx.coroutines.CoroutineScope

class BinanceExchangeService(
    scope: CoroutineScope,
    repository: BinanceRepository,
    notificationService: TelegramNotificationService
): ExchangeService(scope, repository, notificationService)