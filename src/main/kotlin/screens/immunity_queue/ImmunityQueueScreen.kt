package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ImmunityQueueScreen(private val manager: ImmunityQueueManager) : BaseScreen {

    override val id: String = "immunity_queue"

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", "action:immunity_queue_yes")
                dataButton("Нет", "action:immunity_queue_no")
            }
        }

        val text = manager.getImmunityQueueMessage()
        return ScreenContent(text, keyboard)
    }
}
