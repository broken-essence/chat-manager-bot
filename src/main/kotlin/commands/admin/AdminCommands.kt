package com.ehedgehog.commands.admin

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.loggedCommand
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

typealias FailureHandler = suspend (Reason) -> Boolean

private const val COMMAND_HELP_ADMIN = "help_admin"
private const val COMMAND_STATUS = "status"
private const val COMMAND_WARN = "gmwarn"
private const val COMMAND_UNWARN = "gmunwarn"
private const val COMMAND_GIVE_IMMUNITY = "give_immunity"
private const val COMMAND_GIVE_UNWARN = "give_unwarn"
private const val COMMAND_GIVE_BALANCE = "give_balance"
private const val COMMAND_BAN = "ban"
private const val COMMAND_UNBAN = "unban"
private const val COMMAND_INFO = "gminfo"
private const val COMMAND_BOT_STATS = "bot_stats"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerAdminCommands(manager: AdminManager) {

    onCommand(COMMAND_HELP_ADMIN) { command ->
        loggedCommand(COMMAND_HELP_ADMIN, command.from?.id?.chatId.toString()) {
            command.from?.let {
                if (manager.isAdmin(it.id.chatId.toString())) {
                    ScreenRouter.openScreen(
                        bot, ScreenContext(command.chat.id, it), ScreenIds.HELP, "admin"
                    )
                    return@loggedCommand CommandResult.Success()
                }

                return@loggedCommand CommandResult.Failure(Reason.AccessDenied)
            }

            CommandResult.Failure(Reason.UnexpectedError)
        }
    }

    onCommandWithArgs(COMMAND_STATUS) { it, args ->
        executeAdminCommand(COMMAND_STATUS, bot, it, args) {
            manager.changeUserStatus(it, args[0].toInt())
        }
    }

    onCommandWithArgs(COMMAND_WARN) { it, args ->
        executeAdminCommand(COMMAND_WARN, bot, it, args) {
            manager.giveWarn(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_UNWARN) { it, args ->
        val failureHandler: FailureHandler = { reason ->
            if (reason is Reason.WrongCount) {
                bot.sendMessage(it.chat.id, "Неправильное количество предупреждений.")
                true
            } else false
        }

        executeAdminCommand(COMMAND_UNWARN, bot, it, args, failureHandler) {
            manager.takeWarn(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_GIVE_IMMUNITY) { it, args ->
        executeAdminCommand(COMMAND_GIVE_IMMUNITY, bot, it, args) {
            manager.giveImmunity(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_GIVE_UNWARN) { it, args ->
        executeAdminCommand(COMMAND_GIVE_UNWARN, bot, it, args) {
            manager.giveUnwarn(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_GIVE_BALANCE) { it, args ->
        executeAdminCommand(COMMAND_GIVE_BALANCE, bot, it, args) {
            manager.giveBalance(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_BAN) { it, args ->
        executeAdminCommand(COMMAND_BAN, bot, it, args) {
            manager.setBlocked(it, args.joinToString(" "), true)
        }
    }

    onCommandWithArgs(COMMAND_UNBAN) { it, args ->
        executeAdminCommand(COMMAND_UNBAN, bot, it, args) {
            manager.setBlocked(it, args.joinToString(" "), false)
        }
    }

    onCommandWithArgs(COMMAND_INFO) { it, args ->
        val failureHandler: FailureHandler = { reason ->
            if (reason is Reason.UserNotFound) {
                bot.reply(it, "Не удалось найти информацию о заданном пользователе.")
                true
            } else false
        }
        executeAdminCommand(COMMAND_INFO, bot, it, args, failureHandler) {
            manager.getUserInfo(it, args.getOrNull(0))
        }
    }

    onCommand(COMMAND_BOT_STATS) { command ->
        loggedCommand(COMMAND_BOT_STATS, command.from?.id?.chatId.toString()) {
            val result = manager.getBotUsersStats(command)
            if (result is CommandResult.Success) {
                result.message?.let { bot.sendMessage(command.chat.id, it, MarkdownV2) }
            }
            result
        }
    }

}

@OptIn(RiskFeature::class)
private suspend fun executeAdminCommand(
    name: String,
    bot: TelegramBot,
    command: TextMessage,
    args: Array<String>,
    customFailureHandler: FailureHandler? = null,
    block: suspend () -> CommandResult
) {
    loggedCommand(name, command.from?.id?.chatId.toString(), args) {
        val result = block()
        result.handle(bot, command, args, customFailureHandler)
        result
    }
}

suspend fun CommandResult.handle(
    bot: TelegramBot,
    command: TextMessage,
    args: Array<String>,
    customFailureHandler: FailureHandler? = null
) {
    when (this) {
        is CommandResult.Success -> if (message != null) {
            bot.sendMessage(command.chat.id, message, MarkdownV2)
        }

        is CommandResult.Failure -> {
            if (customFailureHandler != null && customFailureHandler(reason)) return

            when (reason) {
                is Reason.UserNotFound -> {
                    val user = args.getOrNull(0) ?: ""
                    bot.sendMessage(
                        command.chat.id,
                        "Пользователь $user не найден.\nПопробуйте использовать id или ответить на его сообщение."
                    )
                }

                else -> {}
            }
        }
    }
}