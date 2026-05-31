package com.ehedgehog.utils

import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.CommandResult
import org.slf4j.LoggerFactory

object Logger {

    private val logger = LoggerFactory.getLogger("BOT")

    fun actionSuccess(name: String, userId: String) {
        logger.info("[ACTION] {} userId={} SUCCESS", name, userId)
    }

    fun actionFailed(name: String, userId: String, reason: String) {
        logger.info("[ACTION] {} userId={} FAILED {}", name, userId, reason)
    }

    fun screen(name: String, userId: String) {
        logger.info("[SCREEN] {} userId={}", name, userId)
    }

    fun commandSuccess(name: String, from: String, args: String? = null, target: String? = null) {
        val target = if (target != null) " target=$target" else ""
        logger.info("[COMMAND] {}{} from={}{} SUCCESS", name, args ?: "", from, target)
    }

    fun commandFailed(name: String, from: String, reason: String, args: String? = null) {
        logger.info("[COMMAND] {}{} from={} FAILED {}", name, args ?: "", from, reason)
    }

    fun notification(message: String, userId: String) {
        logger.info("[NOTIFICATION] {} userId={}", message, userId)
    }

}

suspend fun loggedAction(name: String, userId: String, action: suspend () -> ActionResult) {
    return try {
        when (val result = action()) {
            is ActionResult.Success -> Logger.actionSuccess(name, userId)
            is ActionResult.Failure -> Logger.actionFailed(name, userId, result.reason.code)
        }
    } catch (e: Exception) {
        Logger.actionFailed(name, userId, e.toString())
        throw e
    }
}

suspend fun loggedCommand(name: String, userId: String, args: Array<String>? = null, command: suspend () -> CommandResult) {
    return try {
        if (AccessManager.isBlocked(userId)) return
        when (val result = command()) {
            is CommandResult.Success -> Logger.commandSuccess(name, userId, args?.contentToString(), result.targetUserId)
            is CommandResult.Failure -> Logger.commandFailed(name, userId, result.reason.code, args?.contentToString())
        }
    } catch (e: Exception) {
        Logger.commandFailed(name, userId, e.toString(), args?.contentToString())
        throw e
    }
}

