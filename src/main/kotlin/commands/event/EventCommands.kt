package com.ehedgehog.commands.event

import com.ehedgehog.AppContext
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.utils.loggedCommand
import com.ehedgehog.utils.PluralsUtil
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.utils.RiskFeature

private const val COMMAND_START_EVENT = "start_event"
private const val COMMAND_STOP_EVENT = "stop_event"
private const val COMMAND_REWARD = "reward"
private const val COMMAND_TAKE = "take"
private const val COMMAND_RATING = "rating"
private const val COMMAND_POINTS = "points"
private const val COMMAND_CLEAR_RATING = "clear_rating"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerEventCommands(manager: EventManager) {

    onCommandWithArgs(COMMAND_START_EVENT) { command, args ->
        loggedCommand(COMMAND_START_EVENT, command.from?.id?.chatId.toString(), args) {
            val result = manager.startEvent(command, args)
            if (result is CommandResult.Success)
                result.message?.let { bot.sendMessage(command.chat.id, it, MarkdownV2) }
            result
        }
    }

    onCommandWithArgs(COMMAND_STOP_EVENT) { command, args ->
        loggedCommand(COMMAND_STOP_EVENT, command.from?.id?.chatId.toString(), args) {
            val result = manager.stopEvent(command)
            if (result is CommandResult.Success)
                result.message?.let { bot.sendMessage(command.chat.id, it) }
            result
        }
    }

    onCommandWithArgs(COMMAND_REWARD) { command, args ->
        loggedCommand(COMMAND_REWARD, command.from?.id?.chatId.toString(), args) {
            val result = manager.giveEventPoints(command, args)

            when (result) {
                is CommandResult.Success -> if (result.message != null) {
                    bot.sendMessage(command.chat.id, result.message, MarkdownV2)
                }
                is CommandResult.Failure ->
                    if (result.reason is Reason.AccessDenied) {
                        bot.reply(command, "В админы метишь, бро?")
                    } else if (result.reason is Reason.EventNotEnabled) {
                        bot.reply(command, "Событие уже завершилось!")
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
                        val pointsName = AppContext.settings.getEventConfig().noun
                        val pointsMany = PluralsUtil.getPlurals(pointsName).many
                        bot.reply(command, "У данного пользователя нет столько $pointsMany!")
                    } else if (result.reason is Reason.AccessDenied) {
                        bot.reply(command, "В админы метишь, бро?")
                    } else if (result.reason is Reason.EventNotEnabled) {
                        bot.reply(command, "Событие уже завершилось!")
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

    onCommand(COMMAND_POINTS) {
        loggedCommand(COMMAND_POINTS, it.from?.id?.chatId.toString()) {
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

}