package com.ehedgehog.screens

import com.ehedgehog.utils.Logger
import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContext
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

    suspend fun openScreen(bot: TelegramBot, context: ScreenContext, screenId: String, data: String? = null) {
        renderScreen(bot, context, screenId, data)
        val name = if (!data.isNullOrEmpty()) screenId.plus("($data)") else screenId
        Logger.screen(name, context.user.id.chatId.toString())
    }

    suspend fun refreshScreen(bot: TelegramBot, context: ScreenContext, data: String? = null) {
        val screenId = context.currentScreenId ?: return
        renderScreen(bot, context, screenId, data)
    }

    private suspend fun renderScreen(bot: TelegramBot, context: ScreenContext, screenId: String, data: String? = null) {
        val screen = screens[screenId]
        val content = screen?.render(context, data) ?: return

        if (context.messageId == null) {
            bot.sendMessage(context.chatId, content.text, MarkdownV2, replyMarkup = content.keyboard)
        } else {
            bot.editMessageText(context.chatId, context.messageId, content.text, MarkdownV2, replyMarkup = content.keyboard)
        }
    }

}