package com.ehedgehog.data

import dev.inmo.tgbotapi.types.CallbackQueryId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageId
import dev.inmo.tgbotapi.types.chat.User

data class ScreenContext(
    val chatId: IdChatIdentifier,
    val user: User,
    val messageId: MessageId? = null,
    val callbackId: CallbackQueryId? = null,
    val currentScreenId: String? = null,
    var callbackAnswered: Boolean = false
)
