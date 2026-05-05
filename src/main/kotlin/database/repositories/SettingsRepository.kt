package com.ehedgehog.database.repositories

import com.ehedgehog.database.Settings
import com.ehedgehog.utils.SettingsManager
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

class SettingsRepository {

    fun get(settingKey: SettingsManager.SettingKey)  = transaction {
        Settings.selectAll()
            .where { Settings.key eq settingKey.key }
            .map { it[Settings.value] }
            .singleOrNull()
    }

    fun set(settingKey: SettingsManager.SettingKey, value: String) = transaction {
        Settings.upsert {
            it[Settings.key] = settingKey.key
            it[Settings.value] = value
        }
    }

}