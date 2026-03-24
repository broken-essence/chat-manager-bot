package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class UnwarnRequestScreen(bot: TelegramBot): BaseScreen {

    override val id: String = "request_unwarn"
    override val manager = UnwarnRequestManager(bot)

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            if (context.messageId == null) {
                row {
                    dataButton("✅ Подтвердить", "action:confirm_unwarn?$data")
                    dataButton("❌ Отклонить", "action:decline_unwarn?$data")
                }
            }
        }

        val text = manager.getUnwarnRequestMessage(context.user.id.chatId.toString(), context.user.firstName)
        return ScreenContent(text, keyboard)
    }
}