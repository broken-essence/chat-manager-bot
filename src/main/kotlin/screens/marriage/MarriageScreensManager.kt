package com.ehedgehog.screens.marriage

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.IdChatIdentifier

class MarriageScreensManager(private val bot: TelegramBot) : BaseUserManager() {

    private val userRepository = UserRepository()

    suspend fun getProposalMessage(chatId: IdChatIdentifier, initiatorId: String, recipientId: String): String {
        val pair = userRepository.getUsersPair(initiatorId, recipientId)
        val initiatorMarkdownLink = createMarkdownLink(pair.firstUser!!.name, pair.firstUser.id)
        val recipientMarkdownLink = if (pair.secondUser == null) {
            val chatUser = bot.getChatUserById(chatId, recipientId.toLong())
            createMarkdownLink(chatUser.firstName, recipientId)
        } else
            createMarkdownLink(pair.secondUser.name, pair.secondUser.id)

        return "\uD83D\uDC8D $recipientMarkdownLink, пользователь $initiatorMarkdownLink делает вам предложение руки и сердца\\!\n" +
                "Согласны ли вы вступить с ним в брак?"
    }

}