package com.ehedgehog.screens.help

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.utils.row

class HelpScreen(private val manager: HelpManager): BaseScreen {

    override val id: String = ScreenIds.HELP

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                urlButton("\uD83D\uDCD6 Полное руководство", System.getenv("GUIDE_URL"))
            }
            if (context.currentScreenId != null) {
                row {
                    dataButton("\uD83D\uDD19 Назад", ScreenIds.START)
                }
            }
        }

        val message = if (data == "admin") manager.getAdminHelpMessage() else manager.getHelpMessage()
        return ScreenContent(message, keyboard)
    }
}