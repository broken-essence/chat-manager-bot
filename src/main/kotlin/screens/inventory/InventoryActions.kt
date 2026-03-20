package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseAction
import com.ehedgehog.screens.ScreenContext

class UseUnwarnAction(private val manager: InventoryManager): BaseAction {

    override val id: String = "action:use_unwarn"

    override suspend fun execute(context: ScreenContext) {
        println("Action: use unwarn")
        manager.useUnwarn(context)
    }
}

class UseImmunityAction(manager: InventoryManager): BaseAction {

    override val id: String = "action:use_immunity"

    override suspend fun execute(context: ScreenContext) {
        println("Action: use immunity")
        TODO("Not yet implemented")
    }
}