package com.ehedgehog

import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ActionRouter
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.utils.AccessManager
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answerCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
import dev.inmo.tgbotapi.utils.RiskFeature

@OptIn(RiskFeature::class)
fun BehaviourContext.registerDataCallbackHandler(bot: TelegramBot) {
    onDataCallbackQuery { callback ->
        if (AccessManager.isBlocked(callback.from.id.chatId.toString())) {
            answerCallbackQuery(callback)
            return@onDataCallbackQuery
        }

        val message = callback.message ?: return@onDataCallbackQuery

        val id = callback.data.substringBefore("?")
        val data = callback.data.substringAfter("?", "")
        val screenId = id.substringAfter(":").substringBefore("/")

        val context = ScreenContext(
            message.chat.id,
            callback.from,
            message.messageId,
            callback.id,
            screenId
        )

        if (callback.data.startsWith("action:")) {
            ActionRouter.executeAction(context, id, data)
        } else
            ScreenRouter.openScreen(bot, context, id, data)

        context.callbackId?.let {
            if (!context.callbackAnswered) {
                answerCallbackQuery(it)
                context.callbackAnswered = true
            }
        }
    }
}