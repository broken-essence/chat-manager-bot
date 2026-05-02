package com.ehedgehog.commands.event

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.loggedCommand
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.utils.RiskFeature

private const val COMMAND_COOKIE = "cookie"
private const val COMMAND_TAKE = "take"
private const val COMMAND_RATING = "rating"
private const val COMMAND_BALANCE = "balance"
private const val COMMAND_CLEAR_RATING = "clear_rating"
private const val COMMAND_HINT = "hint"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerEventCommands(manager: EventManager) {

    onCommandWithArgs(COMMAND_COOKIE) { command, args ->
        loggedCommand(COMMAND_COOKIE, command.from?.id?.chatId.toString(), args) {
            val result = manager.giveEventPoints(command, args)

            when (result) {
                is CommandResult.Success -> if (result.message != null) {
                    bot.sendMessage(command.chat.id, result.message, MarkdownV2)
                }
                is CommandResult.Failure -> if (result.reason is Reason.AccessDenied) {
                    bot.reply(command, "В админы метишь, бро?")
                }
            }

            result
        }
    }

    onCommandWithArgs(COMMAND_TAKE) { command, args ->
        loggedCommand(COMMAND_TAKE, command.from?.id?.chatId.toString(), args) {
            val result = manager.takeEventPoints(command, args)

            when (result) {
                is CommandResult.Success -> if (result.message != null) {
                    bot.sendMessage(command.chat.id, result.message, MarkdownV2)
                }
                is CommandResult.Failure ->
                    if (result.reason is Reason.NotEnoughBalance) {
                        bot.reply(command, "У данного пользователя нет столько печенюшек!")
                    } else if (result.reason is Reason.AccessDenied) {
                        bot.reply(command, "В админы метишь, бро?")
                    }
            }

            result
        }
    }

    onCommand(COMMAND_RATING) {
        loggedCommand(COMMAND_RATING, it.from?.id?.chatId.toString()) {
            val result = manager.getEventPointRating()
            if (result is CommandResult.Success)
                result.message?.let { text -> bot.sendMessage(it.chat.id, text, MarkdownV2) }
            result
        }
    }

    onCommand(COMMAND_BALANCE) {
        loggedCommand(COMMAND_BALANCE, it.from?.id?.chatId.toString()) {
            val result = manager.getPersonalRating(it)
            if (result is CommandResult.Success)
                result.message?.let { text -> bot.reply(it, text, MarkdownV2) }
            result
        }
    }

    onCommand(COMMAND_CLEAR_RATING) {
        loggedCommand(COMMAND_CLEAR_RATING, it.from?.id?.chatId.toString()) {
            val result = manager.clearEventPoints(it)
            if (result is CommandResult.Success)
                result.message?.let { text -> bot.sendMessage(it.chat.id, text) }
            result
        }
    }

    onCommand(COMMAND_HINT) {
        loggedCommand(COMMAND_HINT, it.from?.id?.chatId.toString()) {
            val result = manager.showCommands()
            if (result is CommandResult.Success)
                result.message?.let { text -> bot.sendMessage(it.chat.id, text, MarkdownV2) }
            result
        }
    }

}