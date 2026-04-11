package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class InventoryScreen(private val manager: InventoryManager): BaseScreen {

    override val id: String = "inventory"

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDDFB Снять предупреждение", "action:use_unwarn")
            }
            row {
                dataButton("\uD83D\uDC8A Активировать иммунитет", "action:use_immunity")
            }
            row {
                dataButton("\uD83D\uDD19 Назад", "profile")
            }
        }

        val text = manager.getInventoryMessage(context.user.id.chatId.toString())

        return ScreenContent(text, keyboard)
    }
}