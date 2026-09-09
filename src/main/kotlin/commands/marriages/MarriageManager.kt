package com.ehedgehog.commands.marriages

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.base.getMarriageDuration
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.database.MarriageWithUsers
import com.ehedgehog.database.repositories.MarriageRepository
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class MarriageManager : BaseUserManager() {

    private val userRepository = UserRepository()
    private val marriageRepository = MarriageRepository()

    @OptIn(RiskFeature::class)
    fun propose(command: TextMessage): CommandResult {
        val fromUser = command.from ?: return CommandResult.Failure(Reason.UnexpectedError)
        val repliedUser = command.replyTo?.from ?: return CommandResult.Failure(Reason.WrongData)
        if (repliedUser.id.chatId.toString() != fromUser.id.chatId.toString()) {
            val initiatorUser = userRepository.getUserById(fromUser.id.chatId.toString())
            if (initiatorUser?.hasRing == true) {
                if (marriageRepository.isAlreadyMarried(initiatorUser.id, repliedUser.id.chatId.toString()))
                    return CommandResult.Failure(Reason.NotAvailable)
                return CommandResult.Success(targetUserId = repliedUser.id.chatId.toString())
            }
            return CommandResult.Failure(Reason.NotEnoughItems)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    @OptIn(RiskFeature::class)
    fun divorce(command: TextMessage): CommandResult {
        val fromUser = command.from ?: return CommandResult.Failure(Reason.UnexpectedError)
        marriageRepository.getMarriage(fromUser.id.chatId.toString())
            ?: return CommandResult.Failure(Reason.WrongData)
        return CommandResult.Success()
    }

    fun showMarriageList(): CommandResult {
        val marriageList = marriageRepository.getMarriageList()
        val formattedMarriages = formatMarriageList(marriageList)

        val message = "\uD83D\uDC8D Список браков:\n\n$formattedMarriages\n\n" +
                "_\uD83D\uDCCC Для заключения брака необходимо приобрести кольцо в нашем магазине и сделать избраннику " +
                "предложение с помощью команды `/propose`, в ответ на его сообщение\\._"
        return CommandResult.Success(message)
    }

    private fun formatMarriageList(marriages: List<MarriageWithUsers>): String {
        if (marriages.isEmpty())
            return "Список пуст\\."

        return marriages.mapIndexed { index, users ->
            val firstUserMarkdownLink = createMarkdownLink(users.firstUserName, users.firstUserId)
            val secondUserMarkdownLink = createMarkdownLink(users.secondUserName, users.secondUserId)
            "${index + 1}\\. $firstUserMarkdownLink \uD83D\uDC96 $secondUserMarkdownLink \\(${users.getMarriageDuration()} дн\\.\\)"
         }.joinToString("\n")
    }

}