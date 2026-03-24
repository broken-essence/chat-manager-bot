package com.ehedgehog.screens

import com.ehedgehog.base.BaseAction

object ActionRouter {

    private val actions = mutableMapOf<String, BaseAction>()

    fun registerAction(action: BaseAction) {
        actions[action.id] = action
    }

    fun get(id: String): BaseAction? = actions[id]

    suspend fun executeAction(context: ScreenContext, actionId: String, data: String? = null) {
        val action = actions[actionId]
        action?.execute(context, data)
    }

}