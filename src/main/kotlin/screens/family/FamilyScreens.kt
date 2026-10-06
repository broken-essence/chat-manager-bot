package com.ehedgehog.screens.family

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class FamilyInviteScreen(private val manager: FamilyScreensManager) : BaseScreen {

    override val id: String = ScreenIds.FAMILY_INVITE

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", "${ActionIds.FAMILY_INVITE_ACCEPT}?$data")
                dataButton("Нет", "${ActionIds.FAMILY_INVITE_REJECT}?$data")
            }
        }

        val invitedUserId = data?.substringAfter("&") ?: ""
        val text = manager.getInviteMessage(context.chatId, context.user.id.chatId.toString(), invitedUserId)
        return ScreenContent(text, keyboard)
    }

}

class KickFamilyMemberScreen(private val manager: FamilyScreensManager) : BaseScreen {

    override val id: String = ScreenIds.FAMILY_KICK

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", "${ActionIds.FAMILY_KICK_CONFIRM}?$data")
                dataButton("Нет", "${ActionIds.FAMILY_KICK_DECLINE}?$data")
            }
        }

        val targetUserId = data?.substringAfter("&") ?: ""
        val text = manager.getKickMessage(targetUserId)
        return ScreenContent(text, keyboard)
    }

}

class LeaveFromFamilyScreen(private val manager: FamilyScreensManager) : BaseScreen {

    override val id: String = ScreenIds.FAMILY_LEAVE

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", "${ActionIds.FAMILY_LEAVE_CONFIRM}?$data")
                dataButton("Нет", "${ActionIds.FAMILY_LEAVE_DECLINE}?$data")
            }
        }

        val text = manager.getLeaveMessage(data ?: "")
        return ScreenContent(text, keyboard)
    }

}