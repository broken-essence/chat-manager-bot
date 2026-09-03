package com.ehedgehog.commands.marriages

import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.loggedCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.utils.RiskFeature

private const val COMMAND_PROPOSE = "propose"
private const val COMMAND_DIVORCE = "divorce"
private const val COMMAND_MARRIAGES = "marriages"

@OptIn(RiskFeature::class)
fun BehaviourContext.registerMarriageCommands(manager: MarriageManager) {

    onCommand(COMMAND_PROPOSE) { command ->
        loggedCommand(COMMAND_PROPOSE, command.from?.id?.chatId.toString()) {
            val result = manager.propose(command)
            if (result is CommandResult.Success) {
                command.from?.let {
                    ScreenRouter.openScreen(
                        bot,
                        ScreenContext(command.chat.id, it),
                        ScreenIds.PROPOSAL,
                        result.targetUserId
                    )
                }
            }
            result
        }
    }

    onCommand(COMMAND_DIVORCE) { command ->
        loggedCommand(COMMAND_DIVORCE, command.from?.id?.chatId.toString()) {
            CommandResult.Success()
        }
    }

    onCommand(COMMAND_MARRIAGES) { command ->
        loggedCommand(COMMAND_MARRIAGES, command.from?.id?.chatId.toString()) {
            CommandResult.Success()
        }
    }

}