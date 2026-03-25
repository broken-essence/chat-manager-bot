package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseAction
import com.ehedgehog.screens.ScreenContext

class BuyUnwarnAction(private val manager: ShopManager): BaseAction {

    override val id: String = "action:buy_unwarn"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: buy unwarn")
        manager.buyUnwarn(context)
    }

}

class BuyImmunityAction(private val manager: ShopManager): BaseAction {

    override val id: String = "action:buy_immunity"

    override suspend fun execute(context: ScreenContext, data: String?) {
        println("Action: buy immunity")
        manager.buyImmunity(context)
    }

}