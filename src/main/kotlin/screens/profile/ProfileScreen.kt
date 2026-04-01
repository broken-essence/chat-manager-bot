package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.utils.row

class ProfileScreen(private val manager: ProfileManager): BaseScreen {

    override val id: String = "profile"

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83D\uDCE6 Инвентарь", "inventory")
            }
            row {
                dataButton("\uD83D\uDED2 Магазин", "shop")
            }
            row {
                urlButton("\uD83D\uDCAC Поддержка", System.getenv("SUPPORT_URL"))
            }
        }

        val text = manager.getProfileMessage(context.user)
        return ScreenContent(text, keyboard)
    }

}