package com.ehedgehog.commands.marriages

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
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

    fun divorce(command: TextMessage) {

    }

    fun showMarriageList() {

    }

}