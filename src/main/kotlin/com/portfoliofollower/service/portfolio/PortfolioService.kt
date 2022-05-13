package com.portfoliofollower.service.abstract

import com.portfoliofollower.PORTFOLIO_SCAN_TRESHOLD
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.Portfolio
import kotlinx.coroutines.*

abstract class PortfolioService(
    protected val scope: CoroutineScope,
    protected val portfolioTemplate: AutomatorInfo,
    protected val notificationService: TelegramNotificationService
) {
    var lastFetchedPortfolio: Portfolio? = null
    var portfolioChangeListener: OnPortfolioChangedListener? = null
    var job: Job? = null
    var logCounter = 0

    abstract suspend fun getPortfolio(): Portfolio

    fun startObservation() {
        job = scope.launch {
            while (true) {
                run()
                delay(PORTFOLIO_SCAN_TRESHOLD)
            }
        }
    }

    fun stopObservation() {
        scope.launch {
            job?.cancelAndJoin()
        }
    }

    private suspend fun run() = runCatching {
        val portfolio = getPortfolio();
        lastFetchedPortfolio?.let {
            if (portfolio != it) {
                portfolioChangeListener?.onPortfolioChanged(portfolioTemplate.id, portfolio)
            }
        } ?: let {
            portfolioChangeListener?.onPortfolioChanged(portfolioTemplate.id, portfolio)

        }

        if(logCounter % 20 == 0) {
            println("PORTFOLIO FETCHED $portfolio")
            logCounter = 0
        }

        notificationService.lastPortfolio = portfolio

        logCounter++
        lastFetchedPortfolio = portfolio
    }.onFailure {
        it.printStackTrace()
    }
}

interface OnPortfolioChangedListener {
    suspend fun onPortfolioChanged(templateId: String, portfolio: Portfolio)
}