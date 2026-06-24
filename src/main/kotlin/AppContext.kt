package com.ehedgehog

import com.ehedgehog.config.Config
import com.ehedgehog.database.repositories.SettingsRepository
import com.ehedgehog.utils.SettingsManager
import com.ehedgehog.utils.UserActionsJournal

object AppContext {

    val settings by lazy { SettingsManager.create(SettingsRepository()) }

    lateinit var config: Config
    lateinit var journal: UserActionsJournal

}