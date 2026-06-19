package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.showPopup
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.message.MarkdownV2

class ConfirmUnwarnAction(bot: TelegramBot, private val manager: UnwarnRequestManager) : UnwarnAction(bot) {

    override val id: String = ActionIds.UNWARN_CONFIRM

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.confirmUnwarn(context, data)
        handleActionResult(result, context)
        return result
    }
}

class DeclineUnwarnAction(bot: TelegramBot, private val manager: UnwarnRequestManager) : UnwarnAction(bot) {

    override val id: String = ActionIds.UNWARN_DECLINE

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.declineUnwarn(context, data)
        handleActionResult(result, context)
        return result
    }
}

abstract class UnwarnAction(private val bot: TelegramBot) : BaseAction {

    protected suspend fun handleActionResult(result: ActionResult, context: ScreenContext) {
        when (result) {
            is ActionResult.Success -> if (context.messageId != null && result.data != null) {
                bot.editMessageText(context.chatId, context.messageId, result.data, MarkdownV2)
            }

            is ActionResult.Failure -> if (result.reason is Reason.AccessDenied) {
                bot.showPopup(context, "Недостаточно прав ❌")
            }
        }
    }

}