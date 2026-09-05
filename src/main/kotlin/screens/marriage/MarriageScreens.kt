package com.ehedgehog.screens.marriage

import com.ehedgehog.base.BaseScreen
import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionIds
import com.ehedgehog.screens.ScreenIds
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.utils.row

class ProposalScreen(private val manager: MarriageScreensManager) : BaseScreen {

    override val id: String = ScreenIds.PROPOSAL

    override suspend fun render(context: ScreenContext, data: String?): ScreenContent {
        val keyboard = inlineKeyboard {
            row {
                dataButton("\uD83D\uDC9E Да", "${ActionIds.PROPOSAL_ACCEPT}?$data")
                dataButton("\uD83D\uDC94 Нет", "${ActionIds.PROPOSAL_REJECT}?$data")
            }
        }

        val secondId = data?.substringAfter("&") ?: ""
        val text = manager.getProposalMessage(context.chatId, context.user.id.chatId.toString(), secondId)
        return ScreenContent(text, keyboard)
    }
}