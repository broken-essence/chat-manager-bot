package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ScreenContext

class ImmunityQueueConfirmAction(private val manager: ImmunityQueueManager) : BaseAction {

    override val id: String = "action:immunity_queue_yes"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: immunity queue confirm")
        manager.confirmQueue(context)
    }
}

class ImmunityQueueDeclineAction(private val manager: ImmunityQueueManager) : BaseAction {

    override val id: String = "action:immunity_queue_no"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: immunity queue decline")
        manager.declineQueue(context)
    }
}
