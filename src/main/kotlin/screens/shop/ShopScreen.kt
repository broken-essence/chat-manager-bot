package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ShopScreen(private val manager: ShopManager) : BaseScreen {

    override val id: String = "shop"

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDDFB Снятие предупреждения – $PRICE_UNWARN \uD83D\uDCB8", "action:buy_unwarn")
            }
            row {
                dataButton("\uD83D\uDC8A Иммунитет – $PRICE_IMMUNITY \uD83D\uDCB8", "action:buy_immunity")
            }
            row {
                dataButton("\uD83D\uDD19 Назад", "profile")
            }
        }

        val text = manager.getShopMessage(context.user.id.chatId.toString())

        return ScreenContent(text, keyboard)
    }

}