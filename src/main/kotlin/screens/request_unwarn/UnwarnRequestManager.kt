package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.repositories.UnwarnRequestRepository
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.message.MarkdownV2

class UnwarnRequestManager(private val bot: TelegramBot): BaseUserManager(bot) {

    val unwarnRequestRepository = UnwarnRequestRepository()
    val userRepository = UserRepository()

    fun getUnwarnRequestMessage(userId: String, name: String): String {
        val markdownLink = createMarkdownLink(name, userId)
        return "⚠\uFE0F Пользователь $markdownLink \\[`${userId}`\\] запрашивает снятие варна\\!"
    }

    suspend fun confirmUnwarn(context: ScreenContext, requestId: Int) {
        if (isSeniorAdminOrOwner(context.user.id.chatId.toString())) {
            val request = unwarnRequestRepository.getRequest(requestId) ?: return
            val user = userRepository.getUserById(request.userId) ?: return

            val adminMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
            val newMessage = getUnwarnRequestMessage(user.id, user.name)
                .plus("\n\n✅ *Снятие подтверждено*\n — \uD83D\uDC6E\uD83C\uDFFC $adminMarkdownLink \\[`${context.user.id.chatId}`\\]")
            if (context.messageId != null)
                bot.editMessageText(context.chatId, context.messageId, newMessage, MarkdownV2)
        }
    }

    suspend fun declineUnwarn(context: ScreenContext, requestId: Int) {
        if (isSeniorAdminOrOwner(context.user.id.chatId.toString())) {
            val request = unwarnRequestRepository.getRequest(requestId) ?: return
            val user = userRepository.getUserById(request.userId) ?: return

            val adminMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
            val newMessage = getUnwarnRequestMessage(user.id, user.name)
                .plus("\n\n❌ *Снятие отклонено*\n — \uD83D\uDC6E\uD83C\uDFFC $adminMarkdownLink \\[`${context.user.id.chatId}`\\]")
            if (context.messageId != null)
                bot.editMessageText(context.chatId, context.messageId, newMessage, MarkdownV2)

            userRepository.updateUnwarnCount(user.id, user.unwarns + 1)
        }
    }

}