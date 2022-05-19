package com.portfoliofollower.service.notification

import com.binance.api.client.domain.event.OrderTradeUpdateEvent
import com.binance.api.client.domain.event.UserDataUpdateEvent
import com.google.gson.Gson
import com.portfolioprocessor.model.Portfolio
import java.util.*

abstract class NotificationService(private val gson: Gson) {
    abstract fun sendMessage(mTitle: String, message: String,time: Long = Date().time)

    fun notifyPortfolioChange(portfolio: Portfolio) {
        runCatching {
            sendMessage("LEADER PORTFOLIO CHANGED"," ${portfolio.toMessage()}", Date().time)
        }
    }
    fun notifyOrderChange(response: OrderTradeUpdateEvent, time: Long) {
        sendMessage("LEADER ORDER UPDATE", response.toMessage(), time)
    }
}