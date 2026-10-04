package com.ehedgehog.screens.family

import com.ehedgehog.base.TargetedAction
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ActionRouter
import dev.inmo.tgbotapi.bot.TelegramBot

class AcceptFamilyAction(bot: TelegramBot, private val manager: FamilyScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.FAMILY_INVITE_ACCEPT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.acceptFamily(context, data)
        handleActionResult(result, context)
        return result
    }

}

class RejectFamilyAction(bot: TelegramBot, private val manager: FamilyScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.FAMILY_INVITE_REJECT

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.rejectFamily(context, data)
        handleActionResult(result, context)
        return result
    }

}

class ConfirmFamilyKickAction(bot: TelegramBot, private val manager: FamilyScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.FAMILY_KICK_CONFIRM

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.confirmKick(context, data)
        handleActionResult(result, context)
        return result
    }

}

class DeclineFamilyKickAction(bot: TelegramBot, private val manager: FamilyScreensManager) : TargetedAction(bot) {

    override val id: String = ActionIds.FAMILY_KICK_DECLINE

    override suspend fun execute(context: ScreenContext, data: String?): ActionResult {
        val result = manager.declineKick(context, data)
        handleActionResult(result, context)
        return result
    }

}

fun registerFamilyActions(bot: TelegramBot) {
    val manager = FamilyScreensManager(bot)

    ActionRouter.registerAction(AcceptFamilyAction(bot, manager))
    ActionRouter.registerAction(RejectFamilyAction(bot, manager))
    ActionRouter.registerAction(ConfirmFamilyKickAction(bot, manager))
    ActionRouter.registerAction(DeclineFamilyKickAction(bot, manager))
}