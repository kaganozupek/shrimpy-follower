package com.portfoliofollower.di

import com.binance.api.client.BinanceApiClientFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.portfoliofollower.SHRIMPY_BASE_URL
import com.portfoliofollower.api.ShrimpyApi
import com.portfoliofollower.repository.portfolio.exchange.binance.BinanceRepository
import com.portfoliofollower.repository.portfolio.portfolio.shrimpy.ShrimpyRepository
import com.portfoliofollower.service.notification.DiscordNotificationService
import com.portfoliofollower.service.notification.TelegramNotificationService
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


private val coroutineScopeModule = module {
    factory<Job> { Job() }
    factory { CoroutineScope(Dispatchers.Default + get<Job>()) }
}

private val serviceModule = module{

    single {
        val dotEenv: Dotenv = get()
        TelegramNotificationService(
            dotEenv.get("TELEGRAM_BOT_TOKEN") ?: null,
            dotEenv.get("TELEGRAM_CHAT_ID")?.toLong(),
            get()
        )
    }

}

private val repositoryModule = module {
    factory { ShrimpyRepository(get()) }
    factory {
        val dotEnv: Dotenv = get()
        val gson: Gson = get()
        val excludedCoins: List<String> = runCatching {
            val listOfTestObject: Type = object : TypeToken<List<List<String>?>?>() {}.type
            gson.fromJson<List<String>>(dotEnv.get("EXCLUDED_ASSETS"), listOfTestObject)
        }.getOrNull() ?: listOf()
        BinanceRepository(get(), get(),excludedCoins) }
}

private val netModule = module {
    fun provideRetrofit(
        okhttpClient: OkHttpClient,
        factory: Converter.Factory,
        baseURL: String
    ): Retrofit {
        return Retrofit.Builder()
            .client(okhttpClient)
            .baseUrl(baseURL)
            .addConverterFactory(factory)
            .build()
    }

    single(named("okhttp_shrimpy")) {
        val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
        val okhttpClientBuilder = OkHttpClient().newBuilder()
        okhttpClientBuilder.connectTimeout(60, TimeUnit.SECONDS)
        okhttpClientBuilder.readTimeout(60, TimeUnit.SECONDS)
        okhttpClientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        okhttpClientBuilder.cache(null)
     //`   okhttpClientBuilder.addInterceptor(logger)
        okhttpClientBuilder.build()
    }

    single { GsonBuilder().setPrettyPrinting().create() }

    single<Converter.Factory> { GsonConverterFactory.create(get()) }

    single(named("retrofit_shrimpy")) {
        provideRetrofit(
            get(named("okhttp_shrimpy")),
            get(),
            SHRIMPY_BASE_URL
        )
    }

}

private val apiModule= module {
    single { get<Retrofit>(named("retrofit_shrimpy")).create(ShrimpyApi::class.java)}
}

private val thirdPartySDKModule = module {

    factory { Dotenv.configure()
        .filename(".env")
        .load() }

    factory {
        val dotEnv: Dotenv = get()
        val factory = BinanceApiClientFactory.newInstance(dotEnv.get("BINANCE_API_KEY"), dotEnv.get("BINANCE_API_SECRET"))
        factory.newRestClient()
    }

    factory {
        val dotEnv: Dotenv = get()
        val factory = BinanceApiClientFactory.newInstance(dotEnv.get("BINANCE_API_KEY"), dotEnv.get("BINANCE_API_SECRET"))
        factory.newWebSocketClient()
    }

    factory(named("LeaderBinanceClient")) {
        val dotEnv: Dotenv = get()
        val factory = BinanceApiClientFactory.newInstance(dotEnv.get("LIXIVIA_BINANCE_API_KEY"), dotEnv.get("LIXIVIA_BINANCE_API_SECRET"))
        factory.newRestClient()
    }

    factory(named("LeaderBinanceClientSocket")) {
        val dotEnv: Dotenv = get()
        val factory = BinanceApiClientFactory.newInstance(dotEnv.get("LIXIVIA_BINANCE_API_KEY"), dotEnv.get("LIXIVIA_BINANCE_API_SECRET"))
        factory.newWebSocketClient()
    }

    factory {
        val dotEnv: Dotenv = get()
        DiscordNotificationService(dotEnv.get("DISCORD_BOT_TOKEN"), dotEnv.get("DISCORD_CHANNEL_ID"), get(),get())
    }
}

val allModules = listOf(coroutineScopeModule, serviceModule, repositoryModule, netModule, apiModule,thirdPartySDKModule)