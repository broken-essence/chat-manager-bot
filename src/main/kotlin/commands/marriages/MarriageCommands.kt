package com.ehedgehog.commands.marriages

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.loggedCommand
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.utils.RiskFeature

private const val COMMAND_PROPOSE = "propose"
private const val COMMAND_DIVORCE = "divorce"
private const val COMMAND_MARRIAGES = "marriages"
private const val COMMAND_FAMILY = "family"
private const val COMMAND_FAMILY_ADD = "family_add"
private const val COMMAND_FAMILY_KICK = "family_kick"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerMarriageCommands(manager: MarriageManager) {

    onCommand(COMMAND_PROPOSE) { command ->
        loggedCommand(COMMAND_PROPOSE, command.from?.id?.chatId.toString()) {
            val result = manager.propose(command)
            when (result) {
                is CommandResult.Success -> {
                    command.from?.let {
                        ScreenRouter.openScreen(
                            bot,
                            ScreenContext(command.chat.id, it),
                            ScreenIds.PROPOSAL,
                            "${it.id.chatId}&${result.targetUserId}"
                        )
                    }
                }
                is CommandResult.Failure -> when (result.reason) {
                    is Reason.NotEnoughItems -> {
                        bot.reply(command, "Для предложения необходимо приобрести кольцо \uD83D\uDC8D")
                    }
                    is Reason.NotAvailable -> {
                        bot.reply(command, "\uD83D\uDEAB Невозможно сделать предложение, когда один из участников уже состоит в браке.")
                    }
                    else -> {}
                }
            }
            result
        }
    }

    onCommand(COMMAND_DIVORCE) { command ->
        loggedCommand(COMMAND_DIVORCE, command.from?.id?.chatId.toString()) {
            val result = manager.divorce(command)

            when (result) {
                is CommandResult.Success -> command.from?.let {
                    ScreenRouter.openScreen(
                        bot,
                        ScreenContext(command.chat.id, it),
                        ScreenIds.DIVORCE,
                        it.id.chatId.toString()
                    )
                }
                is CommandResult.Failure -> if (result.reason is Reason.WrongData) {
                    bot.reply(command, "Вы не состоите в браке!")
                }
            }

            result
        }
    }

    onCommand(COMMAND_MARRIAGES) { command ->
        loggedCommand(COMMAND_MARRIAGES, command.from?.id?.chatId.toString()) {
            val result = manager.showMarriageList()
            if (result is CommandResult.Success) {
                result.message?.let { text -> bot.sendMessage(command.chat.id, text, MarkdownV2) }
            }
            result
        }
    }

    onCommand(COMMAND_FAMILY_ADD) { command ->
        loggedCommand(COMMAND_FAMILY_ADD, command.from?.id?.chatId.toString()) {
            val result = manager.inviteToFamily(command)
            when (result) {
                is CommandResult.Success -> command.from?.let {
                    ScreenRouter.openScreen(
                        bot,
                        ScreenContext(command.chat.id, it),
                        ScreenIds.FAMILY_INVITE,
                        "${it.id.chatId}&${result.targetUserId}"
                    )
                }
                is CommandResult.Failure -> when (result.reason) {
                    is Reason.WrongData -> {
                        bot.reply(command, "Выбранный пользователь уже состоит в семье!")
                    }
                    is Reason.NotAvailable -> {
                        bot.reply(
                            command,
                            "Приглашать в семью могут только пользователи, состоящие в браке\\.\n\n" +
                                    "_\uD83D\uDCCC Для заключения брака необходимо приобрести кольцо в нашем магазине и " +
                                    "сделать избраннику предложение с помощью команды `/propose`, в ответ на его сообщение\\._",
                            MarkdownV2
                        )
                    }
                    else -> {}
                }
            }
            result
        }
    }

    onCommand(COMMAND_FAMILY_KICK) { command ->
        loggedCommand(COMMAND_FAMILY_KICK, command.from?.id?.chatId.toString()) {
            val result = manager.kickFromFamily(command)
            when (result) {
                is CommandResult.Success -> command.from?.let {
                    ScreenRouter.openScreen(
                        bot,
                        ScreenContext(command.chat.id, it),
                        ScreenIds.FAMILY_KICK,
                        "${it.id.chatId}&${result.targetUserId}"
                    )
                }
                is CommandResult.Failure -> when (result.reason) {
                    is Reason.AccessDenied -> {
                        bot.reply(command, "Изгонять из семьи могут только ее создатели.")
                    }
                    is Reason.WrongData -> {
                        bot.reply(
                            command,
                            "Пользователь не состоит в вашей семье\\!\n\n" +
                                    "_\uD83D\uDCCC Для приглашения в семью необходимо отправить команду `/family_add` " +
                                    "в ответ на сообщение выбранного пользователя\\. Доступно только для участников, " +
                                    "состоящих в браке\\._",
                            MarkdownV2
                        )
                    }
                    is Reason.NotAvailable -> {
                        bot.reply(
                            command,
                            "Невозможно изгнать из семьи пользователя, с которым вы состоите в браке\\.\n\n" +
                                    "_\uD83D\uDCCC Чтобы развестить, воспользуйтесь командой `/divorce`\\._",
                            MarkdownV2
                        )
                    }
                    else -> {}
                }
            }
            result
        }
    }

    onCommand(COMMAND_FAMILY) { command ->
        loggedCommand(COMMAND_FAMILY, command.from?.id?.chatId.toString()) {
            CommandResult.Success()
        }
    }

}