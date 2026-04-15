package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.showPopup
import dev.inmo.tgbotapi.bot.TelegramBot

class BuyUnwarnAction(bot: TelegramBot, private val manager: ShopManager): ShopAction(bot) {

    override val id: String = "action:shop/buy_unwarn"

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.buyUnwarn(context)
        handleActionResult(result, context)
        return result
    }

}

class BuyImmunityAction(bot: TelegramBot, private val manager: ShopManager): ShopAction(bot) {

    override val id: String = "action:shop/buy_immunity"

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.buyImmunity(context)
        handleActionResult(result, context)
        return result
    }

}

abstract class ShopAction(private val bot: TelegramBot): BaseAction {

    protected suspend fun handleActionResult(result: ActionResult, context: ScreenContext) {
        when (result) {
            is ActionResult.Success -> {
                bot.showPopup(context, "Покупка совершена ✅")
                ScreenRouter.refreshScreen(bot, context)
            }

            is ActionResult.Failure -> if (result.reason is Reason.NotEnoughBalance) {
                bot.showPopup(context, "Недостаточно средств ❌")
            }
        }
    }

}