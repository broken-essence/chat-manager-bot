package com.ehedgehog.screens

import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup

data class ScreenContent(
    val text: String,
    val keyboard: InlineKeyboardMarkup
)