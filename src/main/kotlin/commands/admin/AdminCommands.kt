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

    onCommandWithArgs("give_immun") { it, args ->
        manager.giveImmunity(it, args.joinToString(" "))
    }

    onCommandWithArgs("give_unwarn") { it, args ->
        manager.giveUnwarn(it, args.joinToString(" "))
    }

    onCommandWithArgs("give_balance") { it, args ->
        manager.giveBalance(it, args.joinToString(" "))
    }

}