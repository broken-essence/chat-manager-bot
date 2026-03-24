package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ShopScreen(bot: TelegramBot) : BaseScreen {

    override val id: String = "shop"
    override val manager = ShopManager(bot)

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDDFB Снятие предупреждения – 2 \uD83D\uDCB8", "action:buy_unwarn")
            }
            row {
                dataButton("\uD83D\uDC8A Иммунитет – 6 \uD83D\uDCB8", "action:buy_immunity")
            }
            row {
                dataButton("\uD83D\uDD19 Назад", "profile")
            }
        }

        val text = manager.getShopMessage()

        return ScreenContent(text, keyboard)
    }

}