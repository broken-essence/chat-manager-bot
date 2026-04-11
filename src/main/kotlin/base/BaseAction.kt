package com.ehedgehog.base

import com.ehedgehog.data.ScreenContext

interface BaseAction {
    val id: String
    suspend fun execute(context: ScreenContext, data: String? = null)
}