package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class UnwarnRequestScreen(private val manager: UnwarnRequestManager): BaseScreen {

    override val id: String = ScreenIds.REQUEST_UNWARN

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            if (context.messageId == null) {
                row {
                    dataButton("✅ Подтвердить", "${ActionIds.UNWARN_CONFIRM}?$data")
                    dataButton("❌ Отклонить", "${ActionIds.UNWARN_DECLINE}?$data")
                }
            }
        }

        val text = manager.getUnwarnRequestMessage(context.user.id.chatId.toString(), context.user.firstName)
        return ScreenContent(text, keyboard)
    }
}