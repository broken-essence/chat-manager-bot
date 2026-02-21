package com.ehedgehog.commands.general

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs

fun BehaviourContext.registerGeneralCommands(manager: GeneralManager) {

    onCommand("prof") {
        manager.getProfile(it)
    }

    //TODO: move to admin module
    onCommandWithArgs("status") { it, args ->
        manager.changeUserStatus(it, args[0].toInt())
    }

}