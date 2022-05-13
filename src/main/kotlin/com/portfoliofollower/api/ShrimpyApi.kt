package com.portfoliofollower.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ShrimpyApi {
//https://dashboard.shrimpy.io/api/leader_full_details?leaderExchangeAccountId=97051
    @GET("leader_full_details")
    suspend fun getLeaderProfile(
        @Query("leaderExchangeAccountId") userId: String
    ): ShrimpyLeaderDTO
}