package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ScreenContext

class UseUnwarnAction(private val manager: InventoryManager): BaseAction {

    override val id: String = "action:use_unwarn"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: use unwarn")
        manager.useUnwarn(context)
    }
}

class UseImmunityAction(private val manager: InventoryManager): BaseAction {

    override val id: String = "action:use_immunity"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: use immunity")
        manager.useImmunity(context)
    }
}