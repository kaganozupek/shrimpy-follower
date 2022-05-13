package com.portfoliofollower.repository.portfolio

import com.portfolioprocessor.model.Portfolio

interface PortfolioRepository {
    suspend fun getRepository(userId: String, portfolioName: String): Portfolio
}