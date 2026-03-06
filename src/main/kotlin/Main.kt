package com.ehedgehog

import com.ehedgehog.commands.admin.AdminManager
import com.ehedgehog.commands.admin.registerAdminCommands
import com.ehedgehog.commands.event.EventManager
import com.ehedgehog.commands.event.registerEventCommands
import com.ehedgehog.commands.general.GeneralManager
import com.ehedgehog.commands.general.registerGeneralCommands
import com.ehedgehog.config.Config
import com.ehedgehog.database.UserDatabase
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.getUpdates
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UpdateId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.PreviewUser
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.io.File

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
    val manager = EventManager(bot)

    UserDatabase.init()

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

        registerEventCommands(manager)

        registerGeneralCommands(GeneralManager(bot))

        registerAdminCommands(AdminManager(bot))

        onText { message ->
            manager.handleRPCommands(message)
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