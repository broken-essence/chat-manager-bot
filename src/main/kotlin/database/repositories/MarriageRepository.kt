package com.ehedgehog.database.repositories

import com.ehedgehog.database.MarriageWithUsers
import com.ehedgehog.database.Marriages
import com.ehedgehog.database.Users
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class MarriageRepository {

    fun marry(firstUserId: String, secondUserId: String) = transaction {
        Marriages.insert {
            it[Marriages.firstPartnerId] = minOf(firstUserId, secondUserId)
            it[Marriages.secondPartnerId] = maxOf(firstUserId, secondUserId)
            it[Marriages.marriedAt] = System.currentTimeMillis()
        }
    }

    fun divorce(userId: String): Boolean = transaction {
        Marriages.deleteWhere {
            (Marriages.firstPartnerId eq userId) or (Marriages.secondPartnerId eq userId)
        } > 0
    }

    fun isAlreadyMarried(firstUserId: String, secondUserId: String): Boolean = transaction {
        Marriages.selectAll()
            .where { (Marriages.firstPartnerId eq firstUserId) or (Marriages.firstPartnerId eq secondUserId) or
                    (Marriages.secondPartnerId eq firstUserId) or (Marriages.secondPartnerId eq secondUserId)
            }
            .any()
    }

    fun getMarriage(userId: String): MarriageWithUsers? = transaction {
        val firstUser = Users.alias("user1")
        val secondUser = Users.alias("user2")

        Marriages
            .join(firstUser, JoinType.INNER, Marriages.firstPartnerId, firstUser[Users.userId])
            .join(secondUser, JoinType.INNER, Marriages.secondPartnerId, secondUser[Users.userId])
            .selectAll()
            .where {
                (Marriages.firstPartnerId eq userId) or (Marriages.secondPartnerId eq userId)
            }
            .map {
                MarriageWithUsers(
                    firstUserId = it[Marriages.firstPartnerId],
                    firstUserName = it[firstUser[Users.name]],
                    secondUserId = it[Marriages.secondPartnerId],
                    secondUserName = it[secondUser[Users.name]],
                    marriedAt = it[Marriages.marriedAt]
                )
            }
            .singleOrNull()
    }

    fun getMarriageList(): List<MarriageWithUsers> = transaction {
        val firstUser = Users.alias("user1")
        val secondUser = Users.alias("user2")

        Marriages
            .join(firstUser, JoinType.INNER, Marriages.firstPartnerId, firstUser[Users.userId])
            .join(secondUser, JoinType.INNER, Marriages.secondPartnerId, secondUser[Users.userId])
            .select(
                Marriages.firstPartnerId,
                firstUser[Users.name],
                Marriages.secondPartnerId,
                secondUser[Users.name],
                Marriages.marriedAt
            )
            .orderBy(Marriages.marriedAt, SortOrder.ASC)
            .map {
                MarriageWithUsers(
                    firstUserId = it[Marriages.firstPartnerId],
                    firstUserName = it[firstUser[Users.name]],
                    secondUserId = it[Marriages.secondPartnerId],
                    secondUserName = it[secondUser[Users.name]],
                    marriedAt = it[Marriages.marriedAt]
                )
            }
    }

}