package com.ehedgehog.data

import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup

data class ScreenContent(
    val text: String,
    val keyboard: InlineKeyboardMarkup
)