package com.ehedgehog.base

import com.ehedgehog.data.ScreenContent
import com.ehedgehog.data.ScreenContext

interface BaseScreen {
    val id: String
    suspend fun render(context: ScreenContext, data: String? = null): ScreenContent
}