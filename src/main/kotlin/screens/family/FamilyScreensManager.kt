package com.ehedgehog.screens.family

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.MarriageRepository
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.IdChatIdentifier

class FamilyScreensManager(private val bot: TelegramBot): BaseUserManager() {

    private val userRepository = UserRepository()
    private val marriageRepository = MarriageRepository()

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

        return "\uD83E\uDD7E Вы действительно хотите изгнать $markdownLink из семьи?"
    }

    fun getLeaveMessage(userId: String): String {
        val user = userRepository.getUserById(userId)
        val markdownLink = createMarkdownLink(user!!.name, user.id)

        return "\uD83D\uDC94 $markdownLink, вы действительно хотите покинуть свою семью?"
    }

    fun acceptFamily(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)
            val marriage = marriageRepository.getMarriage(ids[0]) ?: return ActionResult.Failure(Reason.WrongData)

            if (context.user.id.chatId.toString() == ids[1]) {
                val user = userRepository.getUserById(ids[1]) ?: UserEntity(
                    ids[1],
                    context.user.firstName,
                    context.user.username?.username ?: ""
                )

                if (user.familyId == null) {
                    updateUserEntry(user.copy(familyId = marriage.familyId))

                    val userMarkdownLink = createMarkdownLink(user.name, user.id)
                    val firstPartnerMarkdownLink = createMarkdownLink(marriage.firstUserName, marriage.firstUserId)
                    val secondPartnerMarkdownLink = createMarkdownLink(marriage.secondUserName, marriage.secondUserId)
                    return ActionResult.Success(
                        "Пользователь $userMarkdownLink присоединился к семье $firstPartnerMarkdownLink " +
                                "и $secondPartnerMarkdownLink\\."
                    )
                }
                return ActionResult.Failure(Reason.WrongData)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun rejectFamily(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)

            if (context.user.id.chatId.toString() == ids[1]) {
                val targetUserMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
                val message = "Пользователь $targetUserMarkdownLink отказался вступать в семью \uD83D\uDE14"
                return ActionResult.Success(message)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun confirmKick(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)

            if (context.user.id.chatId.toString() == ids[0]) {
                val marriage = marriageRepository.getMarriage(ids[0]) ?: return ActionResult.Failure(Reason.WrongData)
                val targetUser = userRepository.getUserById(ids[1]) ?: return ActionResult.Failure(Reason.WrongData)

                if (targetUser.familyId != marriage.familyId)
                    return ActionResult.Failure(Reason.NotAvailable)

                updateUserEntry(targetUser.copy(familyId = null))
                val targetUserMarkdownLink = createMarkdownLink(targetUser.name, targetUser.id)
                return ActionResult.Success("Пользователь $targetUserMarkdownLink больше не состоит в семье\\.")
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun declineKick(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)

            if (context.user.id.chatId.toString() == ids[0]) {
                val initiator = userRepository.getUserById(ids[0])
                val targetUser = userRepository.getUserById(ids[1])

                if (initiator == null || targetUser == null)
                    return ActionResult.Failure(Reason.UnexpectedError)

                val marriage = marriageRepository.getMarriage(initiator.id)
                if (targetUser.familyId != marriage?.familyId)
                    return ActionResult.Failure(Reason.WrongData)

                val initiatorMarkdownLink = createMarkdownLink(initiator.name, initiator.id)
                val targetUserMarkdownLink = createMarkdownLink(targetUser.name, targetUser.id)

                return ActionResult.Success(
                    "$initiatorMarkdownLink решил оставить $targetUserMarkdownLink в семье\\."
                )
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun confirmLeave(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            if (context.user.id.chatId.toString() == it) {
                val user = userRepository.getUserById(it) ?: return ActionResult.Failure(Reason.UnexpectedError)
                if (user.familyId != null) {
                    updateUserEntry(user.copy(familyId = null))
                    val markdownLink = createMarkdownLink(user.name, user.id)
                    return ActionResult.Success("\uD83D\uDC94 $markdownLink покинул семью\\.")
                }

                return ActionResult.Failure(Reason.WrongData)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun declineLeave(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            if (context.user.id.chatId.toString() == it) {
                val user = userRepository.getUserById(it) ?: return ActionResult.Failure(Reason.UnexpectedError)
                if (user.familyId != null) {
                    val markdownLink = createMarkdownLink(user.name, user.id)
                    return ActionResult.Success("$markdownLink решил остаться в семье\\.")
                }

                return ActionResult.Failure(Reason.WrongData)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

}