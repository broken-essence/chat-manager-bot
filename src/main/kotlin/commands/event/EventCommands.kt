package com.ehedgehog.commands.event

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs

fun BehaviourContext.registerEventCommands(manager: EventManager) {

    onCommandWithArgs("cookie") { it, args ->
        if (args.isEmpty())
            manager.giveEventPoints(it)
        else
            manager.giveEventPoints(it, args[0].toInt())
    }

    onCommandWithArgs("take") { it, args ->
        if (args.isEmpty())
            manager.takeEventPoints(it)
        else
            manager.takeEventPoints(it, args[0].toInt())
    }

    onCommand("rating") {
        manager.getEventPointRating(it)
    }

    onCommand("balance") {
        manager.getPersonalRating(it)
    }

    onCommand("clear_rating") {
        manager.clearEventPoints(it)
    }

    onCommand("hint") {
        manager.getCommands(it)
    }

}