package com.ehedgehog

import com.ehedgehog.config.LocalConfig
import com.ehedgehog.config.ProductionConfig
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.DatabaseFactory
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.utils.AccessManager
import com.ehedgehog.utils.ImmunityScheduler
import com.ehedgehog.utils.UserActionsJournal
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.getUpdates
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UpdateId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.util.logging.LogManager

@OptIn(PreviewFeature::class, RiskFeature::class)
suspend fun main(args: Array<String>) {

    println("=== BOT PROCESS STARTED ===")

    val isProduction = System.getenv("APP_CONFIG") == "prod"
    AppContext.config = if (isProduction) {
        ProductionConfig()
    } else {
        val json = Json { ignoreUnknownKeys = true }
        val configFile = File(args.first())
        json.decodeFromString(LocalConfig.serializer(), configFile.readText())
    }

    val bot = telegramBot(AppContext.config.token)

    val scope = CoroutineScope(Dispatchers.Default)
    val userRepository = UserRepository()
    val immunityScheduler = ImmunityScheduler(bot, userRepository, scope)

    AppContext.journal = UserActionsJournal.create(bot)

    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()

    Flyway.configure()
        .dataSource(AppContext.config.databaseUrl, AppContext.config.user, AppContext.config.password)
        .baselineOnMigrate(true)
        .baselineVersion("0")
        .load()
        .migrate()

    DatabaseFactory.init()
    immunityScheduler.restoreNotifications()
    AccessManager.init(userRepository.loadBlockedUsers())

    bot.skipOldUpdates()
    bot.buildBehaviourWithLongPolling(scope,
        defaultExceptionsHandler = { e ->
            println("Connection error: ${e.toString()}")
            delay(10000)
        }) {
        println(">>> LONG POLLING BEHAVIOUR STARTED")
        val me = getMe()

        if (!isProduction) {
            onText { message ->
                println("TEXT RECEIVED: ${message.content.text}")
            }
        }

        registerCommands(bot, this)
        registerScreens(bot)
        registerActions(bot)

        registerDataCallbackHandler(bot)
        registerChatMemberStatusHandler()

        println(me)
    }.join()

    delay(Long.MAX_VALUE)

    println(">>> LONG POLLING STOPPED")

}

suspend fun TelegramBot.skipOldUpdates() {
    val updates = this.getUpdates(limit = 1, offset = UpdateId(-1))

    if (updates.isNotEmpty()) {
        val lastId = updates.first().updateId
        this.getUpdates(offset = lastId + 1)
    }
}

suspend fun TelegramBot.getChatUserById(chatId: ChatIdentifier, userId: Long): User =
    getChatMember(chatId, UserId(RawChatId(userId))).user

suspend fun TelegramBot.showPopup(context: ScreenContext, text: String, showAlert: Boolean = false) {
    context.callbackId?.let {
        answerCallbackQuery(it, text, showAlert)
        context.callbackAnswered = true
    }
}