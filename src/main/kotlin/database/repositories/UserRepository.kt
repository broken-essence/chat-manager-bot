package com.ehedgehog.database.repositories

import com.ehedgehog.database.BotUsersStats
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.Users
import com.ehedgehog.database.UsersPair
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

class UserRepository {

    fun updateUserEntry(user: UserEntity) {
        transaction {
            Users.upsert {
                it[userId] = user.id
                it[name] = user.name
                it[username] = user.username
                it[eventPoints] = user.eventPoints
                it[status] = user.status.ordinal
                it[warns] = user.adminWarns
                it[immunities] = user.immunities
                it[unwarns] = user.unwarns
                it[balance] = user.balance
                it[immunityExpiresAt] = user.immunityExpiresAt
                it[isBlocked] = user.isBlocked
                it[isActive] = user.isActive
                it[hasRing] = user.hasRing
            }
        }
    }

    fun updateUnwarnCount(userId: String, count: Int) {
        transaction {
            Users.update({ Users.userId eq userId }) {
                it[unwarns] = count
            }
        }
    }

    fun getUserById(id: String): UserEntity? {
        return transaction {
            getUserWhere(Users.userId eq id)
        }
    }

    fun getUserByUsername(username: String): UserEntity? {
        return transaction {
            getUserWhere(Users.username eq username)
        }
    }

    fun getUserStatusById(id: String): UserStatus {
        return transaction {
            Users.select(Users.status)
                .where(Users.userId eq id)
                .map { UserStatus.fromInt(it[Users.status]) }
                .singleOrNull() ?: UserStatus.PLAYER
        }
    }

    fun getUsersWithActiveImmunity(): List<UserEntity> {
        return transaction {
            Users.selectAll()
                .where { Users.immunityExpiresAt greaterEq System.currentTimeMillis() }
                .orderBy(Users.immunityExpiresAt)
                .map { it.toUserEntity() }
        }
    }

    fun getUsersPair(firstId: String, secondId: String): UsersPair {
        return transaction {
            val userList = Users.selectAll()
                .where { Users.userId inList listOf(firstId, secondId) }
                .map { it.toUserEntity() }

            UsersPair(
                userList.firstOrNull { it.id == firstId },
                userList.firstOrNull {  it.id == secondId }
            )
        }
    }

    fun hasActivatedBot(id: String): Boolean {
        return transaction {
            Users.select(Users.isActive)
                .where(Users.userId eq id)
                .map { it.getOrNull(Users.isActive) }
                .singleOrNull() ?: false
        }
    }

    fun setActivated(id: String, isActivated: Boolean) {
        transaction {
            Users.update({ Users.userId eq id }) {
                it[Users.isActive] = isActivated
            }
        }
    }

    fun getEventPointCountById(userId: String): Int {
        return transaction {
            Users.select(Users.eventPoints)
                .where(Users.userId eq userId)
                .singleOrNull()?.get(Users.eventPoints) ?: 0
        }
    }

    fun getTopByEventPoints(): List<UserEntity> {
        return transaction {
            Users.selectAll()
                .where { Users.eventPoints greater 0 }
                .orderBy(Users.eventPoints, SortOrder.DESC)
                .limit(20)
                .map {
                    UserEntity(
                        id = it[Users.userId],
                        name = it[Users.name],
                        eventPoints = it[Users.eventPoints]
                    )
                }
        }
    }

    fun clearEventPoints() = transaction {
            Users.update {
                it[Users.eventPoints] = 0
            }
        }

    fun loadBlockedUsers(): List<String> = transaction {
        Users.selectAll()
            .where { Users.isBlocked eq true }
            .map { it[Users.userId] }
    }
    
    fun getBotUsersStats(): BotUsersStats = transaction {
        BotUsersStats(
            totalUsers = Users.selectAll().count(),
            activeUsers = Users.selectAll()
                .where { Users.isActive eq true }
                .count(),
        )
    }

    private fun getUserWhere(predicate: Op<Boolean>): UserEntity? {
        return Users.selectAll()
            .where(predicate)
            .map { it.toUserEntity() }
            .singleOrNull()
    }

}

fun ResultRow.toUserEntity(): UserEntity = UserEntity(
    id = this[Users.userId],
    name = this[Users.name],
    username = this[Users.username],
    eventPoints = this[Users.eventPoints],
    status = UserStatus.fromInt(this[Users.status]),
    adminWarns = this[Users.warns],
    immunities = this[Users.immunities],
    unwarns = this[Users.unwarns],
    balance = this[Users.balance],
    immunityExpiresAt = this[Users.immunityExpiresAt],
    isBlocked = this[Users.isBlocked],
    isActive = this[Users.isActive],
    hasRing = this[Users.hasRing]
)