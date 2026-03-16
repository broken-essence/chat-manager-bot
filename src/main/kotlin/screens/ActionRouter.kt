package com.ehedgehog.screens

import com.ehedgehog.base.BaseAction
import dev.inmo.tgbotapi.bot.TelegramBot

object ActionRouter {

    private val actions = mutableMapOf<String, BaseAction>()

    fun registerAction(action: BaseAction) {
        actions[action.id] = action
    }

    fun get(id: String): BaseAction? = actions[id]

    suspend fun executeAction(bot: TelegramBot, context: ScreenContext, actionId: String) {
        val action = actions[actionId]
        action?.execute(context)
    }

}