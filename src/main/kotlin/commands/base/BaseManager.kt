package com.ehedgehog.commands.base

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId

abstract class BaseManager(private val bot: TelegramBot) {

    suspend fun isAdmin(chatId: IdChatIdentifier, userId: UserId): Boolean {
        return bot.getChatAdministrators(chatId).any { it.user.id == userId }
    }

    fun createMarkdownLink(name: String, userId: String): String = "[${handleReservedSymbols(name)}](tg://user?id=${userId})"

    fun createAmountString(actionWord: String, amount: Int) = when(amount) {
        1 -> "${actionWord}а 1 печенюшка"
        in 2..4 -> "${actionWord}о $amount печенюшки"
        else -> "${actionWord}о $amount печенюшек"
    }

    fun handleReservedSymbols(text: String): String {
        val reservedChars = listOf(
            '_', '*', '[', ']', '(', ')', '~', '`', '>', '#',
            '+', '-', '=', '|', '{', '}', '.', '!'
        )

        var resultString = text
        for (ch in reservedChars) {
            resultString = resultString.replace(ch.toString(), "\\$ch")
        }

        return resultString
    }

}