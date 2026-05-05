package com.ehedgehog

import com.ehedgehog.database.repositories.SettingsRepository
import com.ehedgehog.utils.SettingsManager

object AppContext {

    val settings by lazy { SettingsManager.create(SettingsRepository()) }

}