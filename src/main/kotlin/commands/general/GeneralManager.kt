package com.ehedgehog.commands.general

import com.ehedgehog.commands.base.BaseManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class GeneralManager(private val bot: TelegramBot): BaseManager(bot) {

    private val repository = GeneralRepository()

    @OptIn(RiskFeature::class)
    suspend fun getProfile(command: TextMessage) {
        val sender = command.from
        if (sender != null) {
            if (command.chat.id.chatId.toString() != sender.id.chatId.toString()) {
                bot.reply(command, "Отправлено в личные сообщения.")
            }

            val user = repository.getUserById(sender.id.chatId.toString())

            bot.sendMessage(
                sender.id,
                """
                |🪿 Пользователь *${handleReservedSymbols(sender.firstName)}*
                |👤 Статус: ${getStatusDescription(user?.status ?: UserStatus.PLAYER)}
                |💰 Ваш баланс: 666 чего\-то
                |
                |🧻 Снятие варна: 1
                |💊 Активация иммунитета: 2
                |Иммунитет: действует до 31\.07\.2048 17:41
                |
                |⚠️ Предупреждения: 1\/6
                """.trimMargin(),
                MarkdownV2
            )
        }
    }

    //TODO: move to admin module
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

    private fun getStatusDescription(status: UserStatus): String {
        return when (status) {
            UserStatus.PLAYER -> "Игрок"
            UserStatus.ADMIN -> "Администратор"
            UserStatus.SENIOR_ADMIN -> "Старший администратор"
        }
    }

}