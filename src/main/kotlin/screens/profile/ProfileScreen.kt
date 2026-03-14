package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ProfileScreen(bot: TelegramBot): BaseScreen {

    override val id: String = "profile"

    override val manager = ProfileManager(bot)

    override suspend fun render(context: ScreenContext): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83D\uDCE6 Инвентарь", "inventory")
            }
            row {
                dataButton("\uD83D\uDED2 Магазин", "shop")
            }
        }

        val text = manager.getProfileMessage(context.user)
        return ScreenContent(text, keyboard)
    }

}