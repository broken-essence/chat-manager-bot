package com.ehedgehog.config

interface Config {
    val token: String
    val systemChatId: String
    val journalChannelId: String
    val botOwnerId: String
    val supportUrl: String
    val guideUrl: String

    val immunityDuration: Long
    val immunitiesCountLimit: Int
    val immunityCooldown: Long

    val databaseUrl: String
    val user: String
    val password: String

    fun connectDatabase()
}