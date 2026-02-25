package com.ehedgehog.commands.admin

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs

fun BehaviourContext.registerAdminCommands(manager: AdminManager) {

    onCommandWithArgs("status") { it, args ->
        manager.changeUserStatus(it, args[0].toInt())
    }

    onCommandWithArgs("admwarn") { it, args ->
        manager.giveWarn(it, args.joinToString(" "))
    }

    onCommandWithArgs("admunwarn") { it, args ->
        manager.takeWarn(it, args.joinToString(" "))
    }

}