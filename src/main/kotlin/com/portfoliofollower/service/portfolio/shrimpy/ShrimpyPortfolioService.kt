package com.portfoliofollower.service.portfolio.shrimpy

import com.portfoliofollower.repository.portfolio.shrimpy.ShrimpyRepository
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.service.abstract.PortfolioService
import com.portfolioprocessor.model.Portfolio
import kotlinx.coroutines.CoroutineScope

class ShrimpyPortfolioService(
    scope: CoroutineScope,
    portfolioTemplate: AutomatorInfo,
    private val repository: ShrimpyRepository
): PortfolioService(scope,portfolioTemplate) {
    override suspend fun getPortfolio(): Portfolio {
       return repository.getRepository(portfolioTemplate.leaderId,portfolioTemplate.portfolioID)
    }
}