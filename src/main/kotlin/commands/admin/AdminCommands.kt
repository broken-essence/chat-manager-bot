package com.ehedgehog.commands.admin

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.loggedCommand
import dev.inmo.tgbotapi.bot.TelegramBot
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
private const val COMMAND_ADMWARN = "admwarn"
private const val COMMAND_ADMUNWARN = "admunwarn"
private const val COMMAND_GIVE_IMMUN = "give_immun"
private const val COMMAND_GIVE_UNWARN = "give_unwarn"
private const val COMMAND_GIVE_BALANCE = "give_balance"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerAdminCommands(manager: AdminManager) {

    onCommand(COMMAND_HELP_ADMIN) { command ->
        loggedCommand(COMMAND_HELP_ADMIN, command.from?.id?.chatId.toString()) {
            command.from?.let {
                if (manager.isAdmin(it.id.chatId.toString())) {
                    ScreenRouter.openScreen(
                        bot, ScreenContext(command.chat.id, it), "help", "admin"
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

    onCommandWithArgs(COMMAND_ADMWARN) { it, args ->
        executeAdminCommand(COMMAND_ADMWARN, bot, it, args) {
            manager.giveWarn(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_ADMUNWARN) { it, args ->
        val failureHandler: FailureHandler = { reason ->
            if (reason is Reason.WrongCount) {
                bot.sendMessage(it.chat.id, "Неправильное количество предупреждений.")
                true
            } else false
        }

        executeAdminCommand(COMMAND_ADMUNWARN, bot, it, args, failureHandler) {
            manager.takeWarn(it, args.joinToString(" "))
        }
    }

    onCommandWithArgs(COMMAND_GIVE_IMMUN) { it, args ->
        executeAdminCommand(COMMAND_GIVE_IMMUN, bot, it, args) {
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