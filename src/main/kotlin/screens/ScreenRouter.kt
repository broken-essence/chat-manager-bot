package com.ehedgehog.screens

import com.ehedgehog.commands.base.BaseScreen
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.message.MarkdownV2

object ScreenRouter {

    private val screens = mutableMapOf<String, BaseScreen>()

    fun registerScreen(screen: BaseScreen) {
        screens[screen.id] = screen
    }

    fun get(id: String): BaseScreen? = screens[id]

    suspend fun openScreen(bot: TelegramBot, context: ScreenContext, screenId: String) {
        val screen = screens[screenId]
        val content = screen?.render(context) ?: return

        if (context.messageId == null) {
            bot.sendMessage(context.user.id, content.text, MarkdownV2, replyMarkup = content.keyboard)
        } else {
            bot.editMessageText(context.user.id, context.messageId, content.text, MarkdownV2, replyMarkup = content.keyboard)
        }
    }

}