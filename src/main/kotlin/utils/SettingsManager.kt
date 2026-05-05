package com.ehedgehog.utils

import com.ehedgehog.database.repositories.SettingsRepository
import kotlin.also

class SettingsManager private constructor(
    private val repository: SettingsRepository
) {

    companion object {
        fun create(repository: SettingsRepository) = SettingsManager(repository)
    }

    enum class SettingKey(val key: String) {
        EVENT_ENABLED("event_enabled")
    }

    private val cachedSettings = mutableMapOf<SettingKey, String>()

    fun isEventEnabled(): Boolean {
        return cachedSettings[SettingKey.EVENT_ENABLED]?.toBoolean() ?: repository.get(SettingKey.EVENT_ENABLED)
            ?.also { cachedSettings[SettingKey.EVENT_ENABLED] = it }
            ?.toBoolean()
        ?: false
    }

    fun setEventEnabled(enabled: Boolean) {
        repository.set(SettingKey.EVENT_ENABLED, enabled.toString())
        cachedSettings[SettingKey.EVENT_ENABLED] = enabled.toString()
    }

}