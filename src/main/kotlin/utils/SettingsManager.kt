package com.ehedgehog.utils

import com.ehedgehog.database.EventConfig
import com.ehedgehog.database.repositories.SettingsRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class SettingsManager private constructor(
    private val repository: SettingsRepository
) {

    companion object {
        fun create(repository: SettingsRepository) = SettingsManager(repository)
    }

    enum class SettingKey(val key: String) {
        EVENT_ENABLED("event_enabled"),
        EVENT_EMOJI("event_emoji"),
        EVENT_NOUN("event_noun")
    }

    private val cachedSettings = mutableMapOf<SettingKey, String>()
    private var cachedEventConfig: EventConfig? = null

    fun getEventConfig(): EventConfig {
        cachedEventConfig?.let { return it }

        val config = EventConfig(
            repository.get(SettingKey.EVENT_ENABLED)?.toBoolean() ?: false,
            repository.get(SettingKey.EVENT_EMOJI) ?: "\uD83E\uDD55",
            repository.get(SettingKey.EVENT_NOUN) ?: "морковка"
        )

        cachedEventConfig = config
        return config
    }

    fun setEventConfig(config: EventConfig) {
        transaction {
            repository.set(SettingKey.EVENT_ENABLED, config.enabled.toString())
            repository.set(SettingKey.EVENT_EMOJI, config.emoji)
            repository.set(SettingKey.EVENT_NOUN, config.noun)
        }

        cachedEventConfig = config
    }

}