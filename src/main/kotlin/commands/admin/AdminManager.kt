package com.ehedgehog.commands.admin

import com.ehedgehog.commands.base.BaseUserManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class AdminManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = AdminRepository()

    @OptIn(RiskFeature::class)
    suspend fun changeUserStatus(command: TextMessage, statusValue: Int) {
        val repliedUser = command.replyTo?.from

        if (repliedUser != null && statusValue in 0..<UserStatus.entries.size) {
            val user = repository.getUserById(repliedUser.id.chatId.toString())
            val markdownNameString = createMarkdownLink(repliedUser.firstName, repliedUser.id.chatId.toString())
            val status = UserStatus.fromInt(statusValue)

            repository.setUserStatus(
                user ?: UserEntity(repliedUser.id.chatId.toString(), repliedUser.firstName),
                status
            )

            bot.sendMessage(
                command.chat.id,
                "$markdownNameString теперь *${getStatusDescription(status)}*",
                MarkdownV2
            )
        }
    }

}