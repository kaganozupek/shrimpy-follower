package com.portfoliofollower.service.portfolio.shrimpy

import com.portfoliofollower.PORTFOLIO_SCAN_TRESHOLD
import com.portfoliofollower.repository.portfolio.portfolio.shrimpy.ShrimpyRepository
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.service.abstract.PortfolioService
import com.portfoliofollower.service.notification.TelegramNotificationService
import com.portfolioprocessor.model.Portfolio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShrimpyPortfolioService(
    scope: CoroutineScope,
    portfolioTemplate: AutomatorInfo,
    private val repository: ShrimpyRepository,
    notificationService: TelegramNotificationService
): PortfolioService(scope,portfolioTemplate,notificationService) {
    override suspend fun getPortfolio(): Portfolio {
       return repository.getRepository(portfolioTemplate.leaderId,portfolioTemplate.portfolioID)
    }

    override fun startObservation() {
        scope.launch {
            while (true) {
                run()
                delay(PORTFOLIO_SCAN_TRESHOLD)
            }
        }
    }
}