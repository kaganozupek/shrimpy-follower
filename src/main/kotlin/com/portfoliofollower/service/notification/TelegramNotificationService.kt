package com.portfoliofollower.service.notification

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
) {
    var lastPortfolio: Portfolio? = null
    private var bot: Bot? = null

    fun notifyPortfolioChange(portfolio: Portfolio) = runCatching {
        bot ?: return@runCatching
        telegramChatId ?: return@runCatching
        bot?.sendMessage(ChatId.fromId(telegramChatId), "PORTFOLIO CHANGED \n\n ${gson.toJson(portfolio)}")
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
            }
        }
    }
}