package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ProfileScreen(private val manager: ProfileManager): BaseScreen {

    override val id: String = ScreenIds.PROFILE

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83D\uDCE6 Инвентарь", ScreenIds.INVENTORY)
            }
            row {
                dataButton("\uD83D\uDED2 Магазин", ScreenIds.SHOP)
            }
        }

        val text = manager.getProfileMessage(context.user)
        return ScreenContent(text, keyboard)
    }

}