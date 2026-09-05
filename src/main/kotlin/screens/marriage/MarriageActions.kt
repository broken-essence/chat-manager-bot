package com.ehedgehog.screens.marriage

import com.ehedgehog.base.BaseAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.showPopup
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.message.MarkdownV2

class AcceptProposalAction(bot: TelegramBot, private val manager: MarriageScreensManager) : MarriageAction(bot) {

    override val id: String = ActionIds.PROPOSAL_ACCEPT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.acceptProposal(context, data)
        handleActionResult(result, context)
        return result
    }

}

class RejectProposalAction(bot: TelegramBot, private val manager: MarriageScreensManager) : MarriageAction(bot) {

    override val id: String = ActionIds.PROPOSAL_REJECT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.rejectProposal(context, data)
        handleActionResult(result, context)
        return result
    }

}

abstract class MarriageAction(private val bot: TelegramBot): BaseAction {

    protected suspend fun handleActionResult(result: ActionResult, context: ScreenContext) {
        when (result) {
            is ActionResult.Success -> if (context.messageId != null && result.data != null) {
                bot.editMessageText(context.chatId, context.messageId, result.data, MarkdownV2)
            }
            is ActionResult.Failure -> when (result.reason) {
                Reason.AccessDenied -> bot.showPopup(context, "Эта кнопка не для вас ❌")
                else -> bot.showPopup(context, "Действие не удалось \uD83D\uDE14")
            }
        }
    }

}