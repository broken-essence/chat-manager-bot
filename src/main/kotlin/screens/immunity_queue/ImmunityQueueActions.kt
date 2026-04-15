package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.message.MarkdownV2

class ImmunityQueueConfirmAction(private val bot: TelegramBot, private val manager: ImmunityQueueManager) : BaseAction {

    override val id: String = "action:immunity_queue/immunity_queue_yes"

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.confirmQueue(context)

        when (result) {
            is ActionResult.Success -> if (context.messageId != null && result.data != null) {
                bot.editMessageText(context.chatId, context.messageId, result.data, MarkdownV2)
            }
            is ActionResult.Failure -> if (result.reason is Reason.NotAvailable) {
                context.messageId?.let { bot.delete(context.chatId, it) }
            }
        }

        return result
    }
}

class ImmunityQueueDeclineAction(private val bot: TelegramBot) : BaseAction {

    override val id: String = "action:immunity_queue/immunity_queue_no"

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        context.messageId?.let { bot.deleteMessage(context.chatId, it) }
        return ActionResult.Success()
    }
}
