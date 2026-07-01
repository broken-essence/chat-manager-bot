package com.ehedgehog.commands.general

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.loggedCommand
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.exceptions.CommonRequestException
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.utils.RiskFeature
import dev.inmo.tgbotapi.utils.row

private const val COMMAND_START = "start"
private const val COMMAND_PROFILE = "gmprofile"
private const val COMMAND_IMMUNITIES = "immunities"
private const val COMMAND_RANDOM = "random"
private const val COMMAND_GIFT = "gift"
private const val COMMAND_HELP = "gmhelp"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerGeneralCommands(manager: GeneralManager) {

    onCommand(COMMAND_START) { command ->
        loggedCommand(COMMAND_START, command.from?.id?.chatId.toString()) {
            val result = manager.showStartScreen(command)
            if (result is CommandResult.Success) {
                command.from?.let {
                    ScreenRouter.openScreen(bot, ScreenContext(it.id, it), ScreenIds.START)
                }
            }
            result
        }
    }

    onCommand(COMMAND_PROFILE) { command ->
        loggedCommand(COMMAND_PROFILE, command.from?.id?.chatId.toString()) {
            command.from?.let {
                try {
                    ScreenRouter.openScreen(bot, ScreenContext(it.id, it), ScreenIds.PROFILE)
                    if (command.chat.id.chatId.toString() != it.id.chatId.toString()) {
                        bot.reply(command, "Отправлено в личные сообщения.")
                    }
                } catch (e: CommonRequestException) {
                    if (e.message?.contains("bot was blocked by the user") == true ||
                        e.message?.contains("can't initiate conversation") == true
                    ) {
                        bot.reply(
                            command,
                            "Для выполнения этой команды активируйте бота в личных сообщениях.",
                            replyMarkup = activationKeyboard(bot))
                    } else throw e
                }

                return@loggedCommand CommandResult.Success()
            }

            CommandResult.Failure(Reason.UnexpectedError)
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

    onCommandWithArgs(COMMAND_GIFT) { it, args ->
        loggedCommand(COMMAND_GIFT, it.from?.id?.chatId.toString(), args) {
            val result = manager.gift(it, args)

            if (result is CommandResult.Success) {
                result.message?.let { text ->
                    bot.sendMessage(it.chat.id, text, MarkdownV2)
                }
            }

            result
        }
    }

    onCommand(COMMAND_HELP) { command ->
        loggedCommand(COMMAND_HELP, command.from?.id?.chatId.toString()) {
            command.from?.let {
                ScreenRouter.openScreen(bot, ScreenContext(command.chat.id, it), ScreenIds.HELP)
                return@loggedCommand CommandResult.Success()
            }

            CommandResult.Failure(Reason.UnexpectedError)
        }
    }

}

private suspend fun activationKeyboard(bot: TelegramBot) = inlineKeyboard {
    val username = bot.getMe().username?.username?.removePrefix("@")
    row {
        urlButton("\uD83E\uDD16 Активировать", "https://t.me/$username?start")
    }
}