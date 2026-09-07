package com.ehedgehog.screens.marriage

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

class MarriageScreensManager(private val bot: TelegramBot) : BaseUserManager() {

    private val userRepository = UserRepository()
    private val marriageRepository = MarriageRepository()

    suspend fun getProposalMessage(chatId: IdChatIdentifier, initiatorId: String, recipientId: String): String {
        val pair = userRepository.getUsersPair(initiatorId, recipientId)
        val initiatorMarkdownLink = createMarkdownLink(pair.firstUser!!.name, pair.firstUser.id)
        val recipientMarkdownLink = if (pair.secondUser == null) {
            val chatUser = bot.getChatUserById(chatId, recipientId.toLong())
            createMarkdownLink(chatUser.firstName, recipientId)
        } else
            createMarkdownLink(pair.secondUser.name, pair.secondUser.id)

        return "\uD83D\uDC8D $recipientMarkdownLink, пользователь $initiatorMarkdownLink делает вам предложение руки и сердца\\!\n\n" +
                "Согласны ли вы вступить с ним в брак?"
    }

    fun getDivorceMessage(userId: String): String {
        val marriage = marriageRepository.getMarriage(userId) ?: return ""
        val partnerMarkdownLink = if (marriage.firstUserId == userId)
            createMarkdownLink(marriage.secondUserName, marriage.secondUserId)
        else
            createMarkdownLink(marriage.firstUserName, marriage.firstUserId)

        return "\uD83D\uDC94 Вы действительно хотите развестись с $partnerMarkdownLink?"
    }

    fun acceptProposal(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)
            val usersPair = userRepository.getUsersPair(ids[0], ids[1])

            if (context.user.id.chatId.toString() == ids[1]) {
                if (!marriageRepository.isAlreadyMarried(ids[0], ids[1])) {
                    if (usersPair.firstUser?.hasRing == true) {
                        val secondUser = usersPair.secondUser ?: run {
                            val newUser = UserEntity(
                                ids[1],
                                context.user.firstName,
                                context.user.username?.username ?: ""
                            )
                            updateUserEntry(newUser)
                            newUser
                        }

                        marriageRepository.marry(usersPair.firstUser.id, secondUser.id)
                        updateUserEntry(usersPair.firstUser.copy(hasRing = false))

                        val firstUserMarkdownLink = createMarkdownLink(usersPair.firstUser.name, usersPair.firstUser.id)
                        val secondUserMarkdownLink = createMarkdownLink(secondUser.name, secondUser.id)
                        val message = "\uD83D\uDC8D Пользователь $secondUserMarkdownLink сказал «Да»\\!\n\n" +
                                "Поздравляем $firstUserMarkdownLink и $secondUserMarkdownLink со свадьбой \uD83D\uDC9E"
                        return ActionResult.Success(message)
                    }

                    return ActionResult.Failure(Reason.NotEnoughItems)
                }

                return ActionResult.Failure(Reason.WrongData)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun rejectProposal(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            val ids = data.split("&")
            if (ids.size < 2) return ActionResult.Failure(Reason.UnexpectedError)

            if (context.user.id.chatId.toString() == ids[1]) {
                val secondUserMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
                val message = "Пользователь $secondUserMarkdownLink отказался вступать в брак \uD83D\uDC94"
                return ActionResult.Success(message)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun confirmDivorce(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            if (context.user.id.chatId.toString() == it) {
                val marriage = marriageRepository.getMarriage(it)
                    ?: return ActionResult.Failure(Reason.WrongData)

                marriageRepository.divorce(it)

                val firstUserMarkdownLink = createMarkdownLink(marriage.firstUserName, marriage.firstUserId)
                val secondUserMarkdownLink = createMarkdownLink(marriage.secondUserName, marriage.secondUserId)
                val message = "Брак пользователей $firstUserMarkdownLink и $secondUserMarkdownLink расторгнут \uD83D\uDC94"
                return ActionResult.Success(message)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

    fun declineDivorce(context: ScreenContext, data: String?): ActionResult {
        data?.let {
            if (context.user.id.chatId.toString() == it) {
                val marriage = marriageRepository.getMarriage(it)
                    ?: return ActionResult.Failure(Reason.WrongData)

                val initiatorMarkdownLink = if (marriage.firstUserId == it)
                    createMarkdownLink(marriage.firstUserName, marriage.firstUserId)
                else
                    createMarkdownLink(marriage.secondUserName, marriage.secondUserId)

                val partnerMarkdownLink = if (marriage.firstUserId != it)
                    createMarkdownLink(marriage.firstUserName, marriage.firstUserId)
                else
                    createMarkdownLink(marriage.secondUserName, marriage.secondUserId)

                val message =
                    "❤\uFE0F\u200D\uD83E\uDE79 $initiatorMarkdownLink решил сохранить брак с пользователем $partnerMarkdownLink"
                return ActionResult.Success(message)
            }

            return ActionResult.Failure(Reason.AccessDenied)
        }

        return ActionResult.Failure(Reason.UnexpectedError)
    }

}