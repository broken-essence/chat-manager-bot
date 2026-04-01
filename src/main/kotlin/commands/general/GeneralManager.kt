package com.ehedgehog.commands.general

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class GeneralManager(private val bot: TelegramBot): BaseUserManager(bot) {

    val repository = UserRepository()

    @OptIn(RiskFeature::class)
    suspend fun showProfile(command: TextMessage) {
        val sender = command.from
        if (sender != null) {
            if (command.chat.id.chatId.toString() != sender.id.chatId.toString()) {
                bot.reply(command, "Отправлено в личные сообщения.")
            }

            ScreenRouter.openScreen(bot, ScreenContext(sender.id, sender), "profile")
        }
    }

    suspend fun showImmunitiesList(command: TextMessage) {
        val immunities = repository.getUsersWithActiveImmunity()
        val immunitiesListString = formatImmunitiesList(immunities)

        bot.sendMessage(
            command.chat.id,
            """
                |*Игроки с активным иммунитетом:*
                |
                |$immunitiesListString
                |
                |❗ Иммунитет обозначается эмодзи 🚩 в нике\. Игроков с иммунитетом *запрещено в первые 2 игровые ночи* убивать и посещать активным ролям\.
                """.trimMargin(),
            MarkdownV2
        )
    }

    private fun formatImmunitiesList(list: List<UserEntity>): String =
        if (list.isNotEmpty()) {
            list.mapIndexed { index, user ->
                "${index + 1}\\. ${createMarkdownLink(user.name, user.id)} — ${getImmunityStatus(user)}"
            }.joinToString("\n")
        } else "Список пуст\\."

}