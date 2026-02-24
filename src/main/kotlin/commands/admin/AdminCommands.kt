package com.ehedgehog.commands.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs

fun BehaviourContext.registerAdminCommands(manager: AdminManager) {

    onCommandWithArgs("status") { it, args ->
        manager.changeUserStatus(it, args[0].toInt())
    }

}