package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.showPopup
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId

class UseUnwarnAction(private val bot: TelegramBot, private val manager: InventoryManager): BaseAction {

    override val id: String = ActionIds.USE_UNWARN

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.useUnwarn(context)

        when (result) {
            is ActionResult.Success -> {
                val chatId = ChatId(RawChatId(System.getenv("SYSTEM_CHAT_ID").toLong()))
                val unwarnContext = ScreenContext(chatId, context.user)
                ScreenRouter.openScreen(bot, unwarnContext, ScreenIds.REQUEST_UNWARN, result.data)
                bot.showPopup(context, "Запрос отправлен админам ✅")
                ScreenRouter.refreshScreen(bot, context)
            }

            is ActionResult.Failure -> if (result.reason is Reason.NotEnoughItems) {
                bot.showPopup(context, "Недостаточно анварнов ❌")
            }
        }

        return result
    }
}

class UseImmunityAction(private val bot: TelegramBot, private val manager: InventoryManager): BaseAction {

    override val id: String = ActionIds.USE_IMMUNITY

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.useImmunity(context)

        when (result) {
            is ActionResult.Success -> {
                bot.showPopup(context, "Иммунитет активирован ✅")
                ScreenRouter.refreshScreen(bot, context)
            }
            is ActionResult.Failure -> {
                if (result.reason is Reason.LimitExceeded) {
                    context.callbackId?.let { bot.answerCallbackQuery(it) }
                    val queueContext = ScreenContext(context.chatId, context.user)
                    ScreenRouter.openScreen(bot, queueContext, ScreenIds.IMMUNITY_QUEUE)
                } else if (result.reason is Reason.NotAvailable) {
                    bot.showPopup(context, "Иммунитет недоступен ☹\uFE0F")
                }
            }
        }

        return result
    }

}