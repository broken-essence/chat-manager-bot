package com.ehedgehog.database.repositories

import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.Users
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert

class UserRepository {

    fun setEventPoints(user: UserEntity) {
        transaction {
            Users.upsert {
                it[userId] = user.id
                it[name] = user.name
                it[count] = user.eventPointCount
            }
        }
    }

    fun updateUserEntry(user: UserEntity) {
        transaction {
            Users.upsert {
                it[userId] = user.id
                it[name] = user.name
                it[username] = user.username
                it[count] = user.eventPointCount
                it[status] = user.status.ordinal
                it[warns] = user.adminWarns
                it[immunities] = user.immunities
                it[unwarns] = user.unwarns
                it[balance] = user.balance
                it[immunityExpiresAt] = user.immunityExpiresAt
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

    fun getEventPointCountById(userId: String): Int {
        return transaction {
            Users.select(Users.count)
                .where(Users.userId eq userId)
                .singleOrNull()?.get(Users.count) ?: 0
        }
    }

    fun getTopByEventPoints(): List<UserEntity> {
        return transaction {
            Users.selectAll()
                .orderBy(Users.count, SortOrder.DESC)
                .limit(20)
                .map {
                    UserEntity(
                        id = it[Users.userId],
                        name = it[Users.name],
                        eventPointCount = it[Users.count]
                    )
                }
        }
    }

    //TODO: need to clear only event points, not users
    fun clearEventPoints() {
        transaction {
            Users.deleteAll()
        }
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
    eventPointCount = this[Users.count],
    status = UserStatus.fromInt(this[Users.status]),
    adminWarns = this[Users.warns],
    immunities = this[Users.immunities],
    unwarns = this[Users.unwarns],
    balance = this[Users.balance],
    immunityExpiresAt = this[Users.immunityExpiresAt]
)