package com.portfoliofollower.service.notification

import com.binance.api.client.domain.event.OrderTradeUpdateEvent
import com.binance.api.client.domain.event.UserDataUpdateEvent
import com.google.gson.Gson
import com.portfolioprocessor.model.Portfolio
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class DiscordNotificationService(
    val botToken: String?,
    val channelId: String?,
    val scope: CoroutineScope,
    val gson: Gson
): NotificationService(gson) {

    override fun sendMessage(mTitle: String,message: String, time: Long) {
        scope.launch {
            runCatching {
                botToken ?: return@launch
                channelId ?: return@launch
                val rest = RestClient(botToken)
                val snowflake = Snowflake(channelId)
                rest.channel.createMessage(snowflake) {
                    embed {
                        color = Color(red = 0, green = 0, blue = 255)
                        description = message
                        title = mTitle
                        timestamp = Instant.fromEpochMilliseconds(time)
                    }
                }
            }
        }
    }

}

fun OrderTradeUpdateEvent.toMessage(): String {
    return "Symbol-> ${this.symbol}\nSide-> ${this.side}\nType-> ${type}\nPrice-> ${price}\nStatus-> ${orderStatus}\nExecutionPrice-> ${priceOfLastFilledTrade}"
}