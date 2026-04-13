package com.ehedgehog

import com.ehedgehog.data.ActionResult
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

    fun command(name: String, from: String, result: String) {
        logger.info("[COMMAND] {} from={} result={}", name, from, result)
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

