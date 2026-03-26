package com.ehedgehog.commands.general

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand

fun BehaviourContext.registerGeneralCommands(manager: GeneralManager) {

    onCommand("prof") {
        manager.showProfile(it)
    }

    onCommand("immunities") {
        manager.showImmunitiesList(it)
    }

}