package com.ehedgehog.commands.general

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs

fun BehaviourContext.registerGeneralCommands(manager: GeneralManager) {

    onCommand("prof") {
        manager.showProfile(it)
    }

    onCommand("immunities") {
        manager.showImmunitiesList(it)
    }

    onCommandWithArgs("random") { it, args ->
        manager.randomize(it, args)
    }

}