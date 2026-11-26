package com.ehedgehog.config

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.jdbc.Database

@Serializable
data class LocalConfig(
    override val token: String,
    override val systemChatId: String,
    override val journalChannelId: String,
    override val botOwnerId: String,
    override val supportUrl: String,
    override val guideUrl: String,
    override val immunityDuration: Long,
    override val immunitiesCountLimit: Int,
    override val immunityCooldown: Long,
    override val databaseUrl: String,
    override val user: String,
    override val password: String,
): Config {
    override fun connectDatabase() {
        Database.connect(databaseUrl, driver = "org.sqlite.JDBC")
    }
}