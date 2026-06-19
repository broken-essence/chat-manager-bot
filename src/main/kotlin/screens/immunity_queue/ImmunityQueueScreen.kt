package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ImmunityQueueScreen(private val manager: ImmunityQueueManager) : BaseScreen {

    override val id: String = ScreenIds.IMMUNITY_QUEUE

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", ActionIds.IMMUNITY_QUEUE_CONFIRM)
                dataButton("Нет", ActionIds.IMMUNITY_QUEUE_DECLINE)
            }
        }

        val text = manager.getImmunityQueueMessage()
        return ScreenContent(text, keyboard)
    }
}
