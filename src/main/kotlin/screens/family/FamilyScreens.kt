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
                dataButton("Да", ActionIds.FAMILY_INVITE_ACCEPT)
                dataButton("Нет", ActionIds.FAMILY_INVITE_REJECT)
            }
        }

        val text = manager.getInviteMessage(context.chatId, context.user.id.chatId.toString(), data ?: "")
        return ScreenContent(text, keyboard)
    }

}

class KickFamilyMemberScreen(private val manager: FamilyScreensManager) : BaseScreen {

    override val id: String = ScreenIds.FAMILY_KICK

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("Да", ActionIds.FAMILY_KICK_CONFIRM)
                dataButton("Нет", ActionIds.FAMILY_KICK_DECLINE)
            }
        }

        val text = manager.getKickMessage(data ?: "")
        return ScreenContent(text, keyboard)
    }

}