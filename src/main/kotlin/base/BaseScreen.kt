package com.ehedgehog.base

import com.ehedgehog.screens.ScreenContent
import com.ehedgehog.screens.ScreenContext

interface BaseScreen {
    val id: String
    suspend fun render(context: ScreenContext, data: String? = null): ScreenContent
}