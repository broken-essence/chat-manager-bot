package com.ehedgehog.screens.family

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.IdChatIdentifier

class FamilyScreensManager(private val bot: TelegramBot): BaseUserManager() {

    val userRepository = UserRepository()

    suspend fun getInviteMessage(chatId: IdChatIdentifier, initiatorId: String, invitedUserId: String): String {
        val initiator = userRepository.getUserById(initiatorId)
        val invitedUser = bot.getChatUserById(chatId, invitedUserId.toLong())
        val initiatorMarkdownLink = createMarkdownLink(initiator!!.name, initiator.id)
        val invitedUserMarkdownLink = createMarkdownLink(invitedUser.firstName, invitedUser.id.chatId.toString())

        return "\uD83E\uDD17 $invitedUserMarkdownLink, пользователь $initiatorMarkdownLink приглашает вас вступить в свою семью\\!" +
                "\n\nПринять приглашение?"
    }

    fun getKickMessage(userId: String): String {
        val user = userRepository.getUserById(userId)
        val markdownLink = createMarkdownLink(user!!.name, user.id)

        return "Вы действительно хотите изгнать $markdownLink из семьи?"
    }

}