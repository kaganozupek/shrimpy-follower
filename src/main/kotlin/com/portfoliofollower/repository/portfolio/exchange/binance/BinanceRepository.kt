package com.portfoliofollower.repository.portfolio.exchange.binance

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.OrderSide
import com.binance.api.client.domain.OrderType
import com.binance.api.client.domain.account.NewOrder
import com.binance.api.client.domain.general.FilterType
import com.binance.api.client.domain.market.TickerPrice
import com.portfoliofollower.repository.portfolio.exchange.ExchangeRepository
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.ExchangePortfolio
import com.portfolioprocessor.model.PortfolioAsset
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.CoroutineScope
import java.util.*

class BinanceRepository(
    private val scope: CoroutineScope,
    private val restClient: BinanceApiRestClient,
    private val excludedCoins: List<String>
): ExchangeRepository {

    val exchangeInfo = restClient.exchangeInfo

    override suspend fun getPortfolio(): ExchangePortfolio {
        val balances = restClient.account.balances.filter { it.free.toDouble() > 0 }
        return ExchangePortfolio(
            balances.map {
                ExchangeAsset(
                    it.asset,
                    it.free.toDouble()
                )
            }
        )
    }

    override suspend fun convertToBtc(source: ExchangeAsset,amount: Double, allSymbols: List<TickerPrice>) {
        val symbol = allSymbols.firstOrNull { listOf("${source.code.uppercase()}BTC", "BTC${source.code.uppercase()}").contains(it.symbol) }
        symbol?.let {
            restClient.newOrder(NewOrder(it.symbol,OrderSide.SELL,OrderType.MARKET,null,calculateAmount(it.symbol,amount)))
        }
    }

    override suspend fun convertToUsdt(symbol: String, amount: Double, allSymbols: List<TickerPrice>) {
        val symbol = allSymbols.firstOrNull { listOf("${symbol.uppercase()}USDT", "USDT${symbol.uppercase()}").contains(it.symbol) }
        symbol?.let {
            restClient.newOrder(NewOrder(it.symbol,OrderSide.SELL,OrderType.MARKET,null,calculateAmount(it.symbol,amount)))
        }
    }


    override suspend fun convertToBusd(symbol: String, amount: Double, allSymbols: List<TickerPrice>) {
        val symbol = allSymbols.firstOrNull { listOf("${symbol.uppercase()}BUSD", "BUSD${symbol.uppercase()}").contains(it.symbol) }
        symbol?.let {
            restClient.newOrder(NewOrder(it.symbol,OrderSide.SELL,OrderType.MARKET,null,calculateAmount(it.symbol,amount)))
        }
    }

    override suspend fun buy(symnbol: String, amount: Double, allSymbols: List<TickerPrice>) {
        val symbol = allSymbols.firstOrNull { listOf("${symnbol.uppercase()}USDT", "USDT${symnbol.uppercase()}").contains(it.symbol) }
        symbol?.let {
            restClient.newOrder(NewOrder(it.symbol,OrderSide.BUY, OrderType.MARKET, null, calculateBuyAmount(it,amount)))
        }
    }

    override suspend fun buyWithPair(pair: String, amount: Double, allSymbols: List<TickerPrice>) {
        val symbol = allSymbols.firstOrNull { it.symbol == pair }
        symbol?.let {
            restClient.newOrder(NewOrder(it.symbol,OrderSide.BUY, OrderType.MARKET, null, calculateBuyAmount(it,amount)))
        }
    }

    fun calculateAmount(symbol: String, amount: Double): String {
        val tickerInfo = exchangeInfo.getSymbolInfo(symbol)
        val lotSize = tickerInfo.filters.first {
            it.filterType == FilterType.LOT_SIZE
        }
        val stepSize = lotSize.stepSize.toDouble()
        val stepCount = (amount / stepSize).toInt()
        val regulatedPrice = stepCount * stepSize
        return String.format("%.${tickerInfo.baseAssetPrecision}f", regulatedPrice).replace(",", ".")
    }

    fun calculateBuyAmount(price: TickerPrice, usdtAmount: Double): String {
        val rawAmount = usdtAmount/price.price.toDouble()
        return calculateAmount(price.symbol, rawAmount)
    }

    override fun getAllSymbols(): List<TickerPrice> {
        return restClient.allPrices.filter { asset -> excludedCoins.none { asset.symbol.contains(it)} }
    }


}