package com.ehedgehog.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database

class ProductionConfig: Config {
    override val token: String = System.getenv("BOT_TOKEN")
    override val systemChatId: String = System.getenv("SYSTEM_CHAT_ID")
    override val journalChannelId: String = System.getenv("JOURNAL_CHANNEL_ID")
    override val botOwnerId: String = System.getenv("BOT_OWNER_ID")
    override val supportUrl: String = System.getenv("SUPPORT_URL")
    override val guideUrl: String = System.getenv("GUIDE_URL")

    override val immunityDuration: Long = 24 * 60 * 60 * 1000
    override val immunitiesCountLimit: Int = 5
    override val immunityCooldown: Long = 24 * 60 * 60 * 1000

    override fun connectDatabase() {
        val host = System.getenv("PGHOST") ?: "localhost"
        val port = System.getenv("PGPORT") ?: "5432"
        val database = System.getenv("PGDATABASE") ?: "postgres"
        val user = System.getenv("PGUSER") ?: "postgres"
        val password = System.getenv("PGPASSWORD") ?: ""

        val config = HikariConfig().apply {
            this.jdbcUrl = "jdbc:postgresql://$host:$port/$database"
            this.username = user
            this.password = password
            this.driverClassName = "org.postgresql.Driver"

            maximumPoolSize = 5
            minimumIdle = 1
            connectionTimeout = 30_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000

            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

        Database.connect(HikariDataSource(config))
    }
}