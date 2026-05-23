package com.ehedgehog.screens.start

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.urlButton
import dev.inmo.tgbotapi.utils.row

class StartScreen(private val manager: StartManager) : BaseScreen {

    override val id: String = "start"

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDEBF Профиль", "profile")
            }
            row {
                urlButton("\uD83D\uDCD6 Полное руководство", System.getenv("GUIDE_URL"))
                dataButton("\uD83E\uDDD1\u200D\uD83D\uDCBB Команды бота", "help")
            }
            row {
                urlButton("\uD83D\uDCAC Поддержка", System.getenv("SUPPORT_URL"))
            }
        }

        val message = manager.getStartMessage(context)
        return ScreenContent(message, keyboard)
    }
}