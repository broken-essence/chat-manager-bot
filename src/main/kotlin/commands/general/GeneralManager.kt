package com.ehedgehog.commands.general

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.screens.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class GeneralManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = GeneralRepository()

    @OptIn(RiskFeature::class)
    suspend fun getProfile(command: TextMessage) {
        val sender = command.from
        if (sender != null) {
            if (command.chat.id.chatId.toString() != sender.id.chatId.toString()) {
                bot.reply(command, "Отправлено в личные сообщения.")
            }

            ScreenRouter.openScreen(bot, ScreenContext(command.chat.id, sender), "profile")
        }
    }

}