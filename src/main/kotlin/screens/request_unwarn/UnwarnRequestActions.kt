package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseAction
import com.ehedgehog.screens.ScreenContext

class ConfirmUnwarnAction(private val manager: UnwarnRequestManager) : BaseAction {

    override val id: String = "action:confirm_unwarn"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: unwarn confirmed")
        manager.confirmUnwarn(context)
    }
}

class DeclineUnwarnAction(private val manager: UnwarnRequestManager) : BaseAction {

    override val id: String = "action:decline_unwarn"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: unwarn declined $data")
        val requestId = data?.toInt() ?: return
        manager.declineUnwarn(context, requestId)
    }
}