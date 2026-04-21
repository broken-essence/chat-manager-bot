package com.ehedgehog.commands.general

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.loggedCommand
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.utils.RiskFeature

private const val COMMAND_PROF = "prof"
private const val COMMAND_IMMUNITIES = "immunities"
private const val COMMAND_RANDOM = "random"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerGeneralCommands(manager: GeneralManager) {

    onCommand(COMMAND_PROF) { command ->
        loggedCommand(COMMAND_PROF, command.from?.id?.chatId.toString()) {
            command.from?.let {
                if (command.chat.id.chatId.toString() != it.id.chatId.toString()) {
                    bot.reply(command, "Отправлено в личные сообщения.")
                }

                ScreenRouter.openScreen(bot, ScreenContext(it.id, it), "profile")
                return@loggedCommand CommandResult.Success()
            }

            CommandResult.Failure(Reason.UserNotFound)
        }
    }

    onCommand(COMMAND_IMMUNITIES) {
        loggedCommand(COMMAND_IMMUNITIES, it.from?.id?.chatId.toString()) {
            val result = manager.showImmunitiesList()
            if (result is CommandResult.Success) {
                result.message?.let { text -> bot.sendMessage(it.chat.id, text, MarkdownV2) }
            }
            result
        }
    }

    onCommandWithArgs(COMMAND_RANDOM) { it, args ->
        loggedCommand(COMMAND_RANDOM, it.from?.id?.chatId.toString(), args) {
            val result = manager.randomize(args)

            when (result) {
                is CommandResult.Success -> if (result.message != null) {
                    bot.reply(it.chat.id, it.messageId, result.message, MarkdownV2)
                }
                is CommandResult.Failure -> {
                    bot.reply(it.chat.id, it.messageId, "Ты делаешь что-то не так \uD83D\uDE10")
                }
            }

            result
        }
    }

}