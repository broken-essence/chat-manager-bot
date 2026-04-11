package com.ehedgehog

import com.ehedgehog.config.Config
import com.ehedgehog.database.DatabaseFactory
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ActionRouter
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.getUpdates
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
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
import org.slf4j.bridge.SLF4JBridgeHandler
import java.io.File
import java.util.logging.LogManager

@OptIn(PreviewFeature::class, RiskFeature::class)
suspend fun main(args: Array<String>) {

    println("=== BOT PROCESS STARTED ===")

    /* for local running */
    val json = Json { ignoreUnknownKeys = true }
    val configFile = File(args.first())
    val config: Config = json.decodeFromString(Config.serializer(), configFile.readText())
    val bot = telegramBot(config.testToken)

    /* running with railway */
//    val bot = telegramBot(System.getenv("BOT_TOKEN"))

    val scope = CoroutineScope(Dispatchers.Default)
    val immunityScheduler = ImmunityScheduler(bot, UserRepository(), scope)

    LogManager.getLogManager().reset()
    SLF4JBridgeHandler.install()

    DatabaseFactory.init()
    immunityScheduler.restoreNotifications()

    bot.skipOldUpdates()
    bot.buildBehaviourWithLongPolling(scope,
        defaultExceptionsHandler = { e ->
            println("Connection error: ${e.toString()}")
            delay(10000)
        }) {
        println(">>> LONG POLLING BEHAVIOUR STARTED")
        val me = getMe()

        onText { message ->
            println("TEXT RECEIVED: ${message.content.text}")
        }

        registerCommands(bot, this)
        registerScreens(bot)
        registerActions(bot)

        onDataCallbackQuery { callback ->
            val message = callback.message ?: return@onDataCallbackQuery
            val context = ScreenContext(message.chat.id, callback.from, message.messageId, callback.id)

            val id = callback.data.substringBefore("?")
            val data = callback.data.substringAfter("?", "")

            if (callback.data.startsWith("action:"))
                ActionRouter.executeAction(context, id, data)
            else
                ScreenRouter.openScreen(bot, context, id, data)

            context.callbackId?.let {
                if (!context.callbackAnswered) {
                    answerCallbackQuery(it)
                    context.callbackAnswered = true
                }
            }
        }

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