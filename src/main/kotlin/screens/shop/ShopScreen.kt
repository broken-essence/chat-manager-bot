package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ShopScreen(private val manager: ShopManager) : BaseScreen {

    override val id: String = ScreenIds.SHOP

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDDFB Снятие предупреждения – $PRICE_UNWARN \uD83D\uDCB8", ActionIds.BUY_UNWARN)
            }
            row {
                dataButton("\uD83D\uDC8A Иммунитет – $PRICE_IMMUNITY \uD83D\uDCB8", ActionIds.BUY_IMMUNITY)
            }
            row {
                dataButton("\uD83D\uDC8D Кольцо для предложения – $PRICE_RING \uD83D\uDCB8", ActionIds.BUY_RING)
            }
            row {
                dataButton("\uD83D\uDD19 Назад", ScreenIds.PROFILE)
            }
        }

        val text = manager.getShopMessage(context.user.id.chatId.toString())

        return ScreenContent(text, keyboard)
    }

}