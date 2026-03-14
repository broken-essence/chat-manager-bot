package com.ehedgehog.base

import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext

interface BaseScreen {
    val id: String
    val manager: BaseUserManager
    suspend fun render(context: ScreenContext): ScreenContent
}