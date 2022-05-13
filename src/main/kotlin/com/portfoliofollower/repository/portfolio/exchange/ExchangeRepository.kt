package com.portfoliofollower.repository.portfolio.exchange

import com.binance.api.client.domain.market.TickerPrice
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio

interface ExchangeRepository {
    suspend fun getPortfolio(): ExchangePortfolio
    suspend fun convertToBtc(source: ExchangeAsset, amount: Double, allSymbols: List<TickerPrice>)
    suspend fun convertToUsdt(symbol: String, amount: Double, allSymbols: List<TickerPrice>)
    suspend fun buy(symbol: String, amount: Double, allSymbols: List<TickerPrice>)
    fun getAllSymbols(): List<TickerPrice>
}