package com.portfoliofollower.repository.portfolio.exchange.binance

import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.OrderSide
import com.binance.api.client.domain.OrderType
import com.binance.api.client.domain.TimeInForce
import com.binance.api.client.domain.account.NewOrder
import com.binance.api.client.domain.event.UserDataUpdateEvent
import com.google.common.truth.Truth.assertThat
import com.portfoliofollower.SHRIMPY_LIXIVA_LEADER_ID
import com.portfoliofollower.SHRIMPY_LIXIVA_PORTFOLIO_ID
import com.portfoliofollower.automator.Automator
import com.portfoliofollower.automator.ShrimpyBinanceAutomator
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.model.AutomatorType
import com.portfoliofollower.repository.portfolio.portfolio.shrimpy.ShrimpyRepository
import com.portfoliofollower.service.exchange.binance.BinanceExchangeService
import com.portfoliofollower.service.notification.DiscordNotificationService
import com.portfoliofollower.service.portfolio.shrimpy.ShrimpyPortfolioService
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
import org.koin.core.qualifier.named
import java.util.*
import java.util.concurrent.CountDownLatch


internal class BinanceRepositoryTest : KoinComponent {

    private val restClient: BinanceApiRestClient by inject()
    private val socketClient: BinanceApiWebSocketClient by inject()

    private val binanceRepository: BinanceRepository by inject()
    private val scope: CoroutineScope by inject()
    private val leaderRestClient: BinanceApiRestClient by inject(named("LeaderBinanceClient"))
    private val leaderSocketClient: BinanceApiWebSocketClient by inject(named("LeaderBinanceClientSocket"))
    private val discordNotificationService: DiscordNotificationService by inject()
    private val exchangeService by lazy {
        BinanceExchangeService(
            scope, repository
        )
    }

    private val shrimpyRepository: ShrimpyRepository by inject()

    private val shrimpyPortfolioService: ShrimpyPortfolioService by lazy {
        ShrimpyPortfolioService(scope, template, shrimpyRepository, get())
    }

    private val template =
        AutomatorInfo(SHRIMPY_LIXIVA_LEADER_ID, SHRIMPY_LIXIVA_PORTFOLIO_ID, AutomatorType.SHRIMPY_BINANCE)
    private val automator by lazy {
        ShrimpyBinanceAutomator(
            template, scope, get(), get()
        )
    }

    val repository by lazy {
        BinanceRepository(get(), restClient, listOf())
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

    @Test
    fun `portfolio service test`() = runBlocking {
        shrimpyPortfolioService.lastFetchedPortfolio = Portfolio(
            listOf(
                PortfolioAsset("LUNA", 33),
                PortfolioAsset("USDT", 66)
            )
        )

        shrimpyPortfolioService.processPortfolio(
            Portfolio(
                listOf(
                    PortfolioAsset("LUNA", 33),
                    PortfolioAsset("BTC", 66)
                )
            )
        )
    }

    @Test
    fun `leader client connection`() {
        leaderRestClient.ping()
    }

    @Test
    fun `leader client balance`() {
        val latch = CountDownLatch(1)
        val listenKey = restClient.startUserDataStream()
        socketClient.onUserDataUpdateEvent(listenKey, object: BinanceApiCallback<UserDataUpdateEvent> {

            override fun onResponse(response: UserDataUpdateEvent?) {
                val x = 0
                val c = x
            }

            override fun onFailure(cause: Throwable?) {
                super.onFailure(cause)
            }
        })
        latch.await()
    }

    @Test
    fun `test discord message`() = runBlocking {
        discordNotificationService.sendMessage("TEST","TEST")
    }
}