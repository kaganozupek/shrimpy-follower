package com.portfoliofollower.api

data class ShrimpyLeaderDTO(
    val profiles: List<ShrimpyProfileDTO>
)

data class ShrimpyProfileDTO(
    val portfolioName: String,
    val assets: List<ShrimpyAssets>
)

data class ShrimpyAssets(
    val currency: String,
    val actualPercent: Float
)