package com.portfoliofollower.repository.portfolio.exchange.binance

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.OrderSide
import com.binance.api.client.domain.OrderType
import com.binance.api.client.domain.TimeInForce
import com.binance.api.client.domain.account.NewOrder
import com.google.common.truth.Truth.assertThat
import com.portfoliofollower.SHRIMPY_LIXIVA_LEADER_ID
import com.portfoliofollower.SHRIMPY_LIXIVA_PORTFOLIO_ID
import com.portfoliofollower.automator.Automator
import com.portfoliofollower.automator.ShrimpyBinanceAutomator
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.model.AutomatorType
import com.portfoliofollower.service.exchange.binance.BinanceExchangeService
import com.portfoliofollower.setupKoin
import com.portfolioprocessor.model.ExchangeAsset
import com.portfolioprocessor.model.Portfolio
import com.portfolioprocessor.model.PortfolioAsset
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject


internal class BinanceRepositoryTest : KoinComponent {

    private val restClient: BinanceApiRestClient by inject()
    private val binanceRepository: BinanceRepository by inject()
    private val scope: CoroutineScope by inject()
    private val exchangeService by lazy {
        BinanceExchangeService(
            scope, repository
        )
    }

    private val template =
        AutomatorInfo(SHRIMPY_LIXIVA_LEADER_ID, SHRIMPY_LIXIVA_PORTFOLIO_ID, AutomatorType.SHRIMPY_BINANCE)
    private val automator by lazy {
        ShrimpyBinanceAutomator(
            template, scope, get()
        )
    }

    val repository by lazy {
        BinanceRepository(get(), restClient)
    }

    @Before
    fun setup() {
        setupKoin()

    }

    @Test
    fun `test client is working`() {
        restClient.ping()
    }

    @Test
    fun `get spot portfolio`() {
        val balances = restClient.account.balances
        assert(balances.isNotEmpty())
    }

    @Test
    fun `convert to USDT`() = runBlocking {
        val portfolio = exchangeService.getPortfolio()
        println(portfolio.toString())
        exchangeService.resetPortfolioToUSDT()
        var newPortfolio = exchangeService.getPortfolio()
        var c = newPortfolio.assets
        var g = exchangeService.getPortfolio()
    }

    @Test
    fun `automator test`() = runBlocking {
        automator.onPortfolioChanged(
            template.id, Portfolio(
                listOf(
                        PortfolioAsset(
                            "BTC", 33
                        ),
                        PortfolioAsset(
                            "BNB", 33
                        ),
                        PortfolioAsset(
                            "LUNA", 33
                        ),
                    )
            )
        )
    }
}