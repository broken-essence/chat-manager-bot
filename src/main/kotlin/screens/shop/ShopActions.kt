package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseAction
import com.ehedgehog.screens.ScreenContext

class BuyUnwarnAction(manager: ShopManager): BaseAction {

    override val id: String = "action:buy_unwarn"

    override suspend fun execute(context: ScreenContext) {
        println("Action: buy unwarn")
        TODO("Not yet implemented")
    }

}

class BuyImmunityAction(manager: ShopManager): BaseAction {

    override val id: String = "action:buy_immunity"

    override suspend fun execute(context: ScreenContext) {
        println("Action: buy immunity")
        TODO("Not yet implemented")
    }

}