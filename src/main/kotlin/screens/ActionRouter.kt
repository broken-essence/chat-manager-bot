package com.ehedgehog.screens

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.utils.loggedAction

object ActionRouter {

    private val actions = mutableMapOf<String, BaseAction>()

    fun registerAction(action: BaseAction) {
        actions[action.id] = action
    }

    fun get(id: String): BaseAction? = actions[id]

    suspend fun executeAction(context: ScreenContext, actionId: String, data: String? = null) {
        var name = actionId.substringAfter("/")
        if (!data.isNullOrEmpty()) name = name.plus("(id$data)")
        loggedAction(name, context.user.id.chatId.toString()) {
            val action = actions[actionId]
            action?.execute(context, data) ?: ActionResult.Failure(Reason.UnexpectedError)
        }
    }

}