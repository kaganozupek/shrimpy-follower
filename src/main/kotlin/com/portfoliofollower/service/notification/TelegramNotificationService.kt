package com.portfoliofollower.service.notification

import com.binance.api.client.domain.event.OrderTradeUpdateEvent
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.google.gson.Gson
import com.portfolioprocessor.model.Portfolio

class TelegramNotificationService(
    private val telegramBotToken: String?,
    private val telegramChatId: Long?,
    private val gson: Gson
): NotificationService(gson) {
    var lastPortfolio: Portfolio? = null
    var rebalance: (() -> Unit)? = null
    private var bot: Bot? = null

    override fun sendMessage(mTitle: String, message: String, time: Long) {
        telegramChatId ?: return
        bot?.sendMessage(ChatId.fromId(telegramChatId), "$mTitle\n\n$message")
    }

    init {
        initialize()
        bot?.startPolling()
    }

    private fun initialize() {
        telegramBotToken ?: return
        telegramChatId ?: return

        bot = bot {
            token = telegramBotToken
            dispatch {
                command("ping") {
                    runCatching {
                        bot.sendMessage(ChatId.fromId(telegramChatId), "Pong!")
                    }
                }

                command("last-portfolio") {
                    runCatching {
                        bot.sendMessage(ChatId.fromId(telegramChatId), gson.toJson(lastPortfolio ?: "{}"))
                    }
                }

                command("rebalance") {
                    rebalance?.invoke()
                }
            }
        }
    }
}