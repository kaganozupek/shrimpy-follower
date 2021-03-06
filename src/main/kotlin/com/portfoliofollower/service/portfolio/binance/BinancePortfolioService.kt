package com.portfoliofollower.service.portfolio.binance

import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.UserDataUpdateEvent
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.repository.portfolio.portfolio.shrimpy.BinancePortfolioRepository
import com.portfoliofollower.service.abstract.PortfolioService
import com.portfoliofollower.service.notification.DiscordNotificationService
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.Portfolio
import kotlinx.coroutines.*
import okhttp3.internal.closeQuietly
import java.io.Closeable

class BinancePortfolioService(
    scope: CoroutineScope,
    info: AutomatorInfo,
    notificationService: TelegramNotificationService,
    private val restClient: BinanceApiRestClient,
    private val socketClient: BinanceApiWebSocketClient,
    private val discordNotificationService: DiscordNotificationService

): PortfolioService(
    scope,info,notificationService
) {
    val repository = BinancePortfolioRepository(restClient)
    var socket: Closeable? = null

    override suspend fun getPortfolio(): Portfolio {
        return repository.getRepository("","")
    }

    override fun startObservation() {
        startSocketListen()
        scope.launch {
         //   runForce()

        }
    }
    var jobRun: Job? = null

    var initializing = false
    private fun startSocketListen() {
        if(initializing) {
            return
        }
        initializing = true
        notificationService.sendMessage("INFO","Socket Initializing")
        val listenKey = restClient.startUserDataStream()

        runCatching {
            socket?.closeQuietly()
        }

        socket = socketClient.onUserDataUpdateEvent(listenKey, object: BinanceApiCallback<UserDataUpdateEvent> {
            override fun onResponse(response: UserDataUpdateEvent?) {
                if(response?.eventType != UserDataUpdateEvent.UserDataUpdateEventType.ORDER_TRADE_UPDATE) {
                    runCatching {
                        jobRun?.cancel()
                    }
                    jobRun = scope.launch {
                        delay(500)
                        run()
                    }
                }
                notifyDiscordChannel(response)
                notifyTelegram(response)
            }

            override fun onFailure(cause: Throwable?) {
                cause?.printStackTrace()
                notificationService.sendMessage("ERROR","Socket Error")
                notificationService.sendMessage("ERROR", "${cause?.toString()}")
                notificationService.sendMessage("ERROR","${cause?.message}")
                startSocketListen()

            }

            override fun onClosing(code: Int?, reason: String?) {
                //notificationService.sendMessage("SOCKET CLOSING", "${code}, $reason")
            }

            override fun onClosed(code: Int?, reason: String?) {
                notificationService.sendMessage("SOCKET CLOSED", "${code}, $reason")
                startSocketListen()
            }

        })

        initializing = false
    }

    private fun notifyTelegram(response: UserDataUpdateEvent?) {
        notificationService.notifyOrderChange(response?.orderTradeUpdateEvent ?: return,response.eventTime)

    }

    private fun notifyDiscordChannel(response: UserDataUpdateEvent?) {
        discordNotificationService.notifyOrderChange(response?.orderTradeUpdateEvent ?: return,response.eventTime)
    }

    private suspend fun restartSocket() {
        delay(10000)
        startSocketListen()
    }


}