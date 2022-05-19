package com.portfoliofollower.repository.portfolio.portfolio.shrimpy

import com.portfoliofollower.api.ShrimpyApi
import com.portfoliofollower.repository.portfolio.portfolio.PortfolioRepository
import com.portfolioprocessor.model.Portfolio
import com.portfolioprocessor.model.toPortfolio

class ShrimpyRepository(
    private val api: ShrimpyApi
): PortfolioRepository {
    override suspend fun getRepository(userId: String, portfolioName: String): Portfolio {
        return api.getLeaderProfile(userId).profiles.first {
            it.portfolioName == portfolioName
        }.toPortfolio()
    }
}