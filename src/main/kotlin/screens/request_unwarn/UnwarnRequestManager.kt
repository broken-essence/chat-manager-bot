package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.MarkdownV2

class UnwarnRequestManager(private val bot: TelegramBot): BaseUserManager(bot) {

    fun getUnwarnRequestMessage(user: User): String {
        val markdownLink = createMarkdownLink(user.firstName, user.id.chatId.toString())
        return "⚠\uFE0F Пользователь ${markdownLink}\\[`${user.id.chatId}`\\] запрашивает снятие варна\\!"
    }

    suspend fun confirmUnwarn(context: ScreenContext) {
        val newMessage = getUnwarnRequestMessage(context.user).plus("\n\n✅ Снятие подтверждено\\.")
        if (context.messageId != null)
            bot.editMessageText(context.chatId, context.messageId, newMessage, MarkdownV2)
    }

    suspend fun declineUnwarn(context: ScreenContext) {
        val newMessage = getUnwarnRequestMessage(context.user).plus("\n\n❌ Снятие отклонено\\.")
        if (context.messageId != null)
            bot.editMessageText(context.chatId, context.messageId, newMessage, MarkdownV2)

        //TODO: return unwarn item to user
    }

}