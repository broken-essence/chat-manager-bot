package com.ehedgehog.commands.general

import com.ehedgehog.commands.base.BaseUserManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
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

            val user = repository.getUserById(sender.id.chatId.toString()) ?: run {
                val newUser = UserEntity(sender.id.chatId.toString(), sender.firstName, sender.username?.username ?: "")
                repository.updateUserEntry(newUser)
                newUser
            }

            bot.sendMessage(
                sender.id,
                """
                |🪿 Пользователь *${handleReservedSymbols(sender.firstName)}*
                |👤 Статус: ${getStatusDescription(user.status)}
                |💰 Ваш баланс: ${user.balance} 💸
                |
                |🧻 Снятие варна: ${user.unwarns}
                |💊 Активация иммунитета: ${user.immunities}
                |Иммунитет: действует до 31\.07\.2048 17:41
                |
                |⚠️ Предупреждения: ${user.adminWarns}\/6
                """.trimMargin(),
                MarkdownV2
            )
        }
    }

}