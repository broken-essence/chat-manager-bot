package com.ehedgehog.screens.marriage

import com.ehedgehog.base.TargetedAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ActionRouter
import dev.inmo.tgbotapi.bot.TelegramBot

class AcceptProposalAction(bot: TelegramBot, private val manager: MarriageScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.PROPOSAL_ACCEPT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.acceptProposal(context, data)
        handleActionResult(result, context)
        return result
    }

}

class RejectProposalAction(bot: TelegramBot, private val manager: MarriageScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.PROPOSAL_REJECT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.rejectProposal(context, data)
        handleActionResult(result, context)
        return result
    }

}

class ConfirmDivorceAction(bot: TelegramBot, private val manager: MarriageScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.DIVORCE_CONFIRM

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.confirmDivorce(context, data)
        handleActionResult(result, context)
        return result
    }

}

class DeclineDivorceAction(bot: TelegramBot, private val manager: MarriageScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.DIVORCE_DECLINE

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.declineDivorce(context, data)
        handleActionResult(result, context)
        return result
    }

}

fun registerMarriageActions(bot: TelegramBot) {
    val manager = MarriageScreensManager(bot)

    ActionRouter.registerAction(AcceptProposalAction(bot, manager))
    ActionRouter.registerAction(RejectProposalAction(bot, manager))
    ActionRouter.registerAction(ConfirmDivorceAction(bot, manager))
    ActionRouter.registerAction(DeclineDivorceAction(bot, manager))
}