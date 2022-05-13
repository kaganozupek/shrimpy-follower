package com.portfoliofollower.service.exchange.binance

import com.portfoliofollower.repository.portfolio.exchange.binance.BinanceRepository
import com.portfoliofollower.service.exchange.ExchangeService
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio
import kotlinx.coroutines.CoroutineScope

class BinanceExchangeService(
    scope: CoroutineScope,
    repository: BinanceRepository
): ExchangeService(scope, repository)