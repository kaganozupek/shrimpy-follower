package com.portfoliofollower

import com.portfoliofollower.automator.Automator
import com.portfoliofollower.automator.ShrimpyBinanceAutomator
import com.portfoliofollower.di.allModules
import com.portfoliofollower.model.AutomatorInfo
import com.portfoliofollower.model.AutomatorType
import com.portfoliofollower.service.notification.TelegramNotificationService
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

val templates = listOf(
    AutomatorInfo(SHRIMPY_LIXIVA_LEADER_ID, SHRIMPY_LIXIVA_PORTFOLIO_ID,AutomatorType.SHRIMPY_BINANCE)
)
val instance = MainInstance()

fun main() {
    instance.start()
    while (true) {

    }
}

class MainInstance: KoinComponent {

    val scope: CoroutineScope by inject()
    val notificationService: TelegramNotificationService by inject()

    fun start() {
        initDI()
        startAutomators()
    }

    private fun startAutomators() {
        templates.map {
            it.createAutomatorByTemplate(scope)
        }.forEach {
           it.startAutomation()
        }
    }

    private fun initDI() {
        setupKoin()
    }




    fun AutomatorInfo.createAutomatorByTemplate(scope: CoroutineScope) : Automator {
        return when (automatorType) {
            AutomatorType.SHRIMPY_BINANCE -> ShrimpyBinanceAutomator(
                this,
                scope,
                notificationService
            )
        }
    }

}

fun setupKoin() {
    runCatching {
        startKoin {
            modules(allModules)
        }
    }.onFailure {
        loadKoinModules(allModules)
    }
}