package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class InventoryScreen(private val manager: InventoryManager): BaseScreen {

    override val id: String = ScreenIds.INVENTORY

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83E\uDDFB Снять предупреждение", ActionIds.USE_UNWARN)
            }
            row {
                dataButton("\uD83D\uDC8A Активировать иммунитет", ActionIds.USE_IMMUNITY)
            }
            row {
                dataButton("\uD83D\uDD19 Назад", ScreenIds.PROFILE)
            }
        }

        val text = manager.getInventoryMessage(context.user.id.chatId.toString())

        return ScreenContent(text, keyboard)
    }
}