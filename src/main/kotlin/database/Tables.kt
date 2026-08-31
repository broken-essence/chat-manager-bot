package com.ehedgehog.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object Users : Table("users") {
    val userId = varchar("user_id", 50).uniqueIndex()
    val name = varchar("name", 50)
    val username = varchar("username", 50).default("")
    val eventPoints = integer("event_points").default(0)
    val status = integer("status").default(UserStatus.PLAYER.ordinal)
    val warns = integer("warns").default(0)
    val immunities = integer("immunities").default(0)
    val unwarns = integer("unwarns").default(0)
    val balance = integer("balance").default(0)
    val immunityExpiresAt = long("immunity_expires_at").default(0)
    val isBlocked = bool("is_blocked").default(false)
    val isActive = bool("is_active").default(false)
    val hasRing = bool("has_ring").default(false)
}

object UnwarnRequests : IntIdTable("unwarn_requests") {
    val userId = varchar("user_id", length = 50)
    val createdAt = long("created_at").default(0)
}

object Marriages : Table("marriages") {
    val firstPartnerId = varchar("first_partner_id", 50)
    val secondPartnerId = varchar("second_partner_id", 50)
    val marriedAt = long("married_at").default(0)

    override val primaryKey = PrimaryKey(firstPartnerId, secondPartnerId)
}

object Settings : Table("settings") {
    val key = varchar("key", 50).uniqueIndex()
    val value = varchar("value", 255)
}