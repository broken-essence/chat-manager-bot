package com.ehedgehog.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object Users : Table("users") {
    val userId = varchar("user_id", 50).uniqueIndex()
    val name = varchar("name", 50)
    val username = varchar("username", 50).default("")
    val count = integer("count").default(0)
    val status = integer("status").default(UserStatus.PLAYER.ordinal)
    val warns = integer("warns").default(0)
    val immunities = integer("immunities").default(0)
    val unwarns = integer("unwarns").default(0)
    val balance = integer("balance").default(0)
}

object UnwarnRequests : IntIdTable("unwarn_requests") {
    val userId = varchar("user_id", length = 50)
    val createdAt = long("created_at").default(0)
}