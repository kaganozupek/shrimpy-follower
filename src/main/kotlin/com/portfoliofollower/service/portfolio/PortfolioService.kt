package com.portfoliofollower.service.abstract

import com.portfoliofollower.PORTFOLIO_SCAN_TRESHOLD
import com.portfoliofollower.model.AutomatorInfo
import com.portfolioprocessor.model.Portfolio
import kotlinx.coroutines.*

abstract class PortfolioService(
    protected val scope: CoroutineScope,
    protected val portfolioTemplate: AutomatorInfo
) {
    var lastFetchedPortfolio: Portfolio? = null
    var portfolioChangeListener: OnPortfolioChangedListener? = null
    var job: Job? = null

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

        println("PORTFOLIO FETCHED $portfolio")
        lastFetchedPortfolio = portfolio
    }.onFailure {
        it.printStackTrace()
    }
}

interface OnPortfolioChangedListener {
    suspend fun onPortfolioChanged(templateId: String, portfolio: Portfolio)
}