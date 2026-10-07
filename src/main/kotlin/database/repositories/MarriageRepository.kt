package com.ehedgehog.database.repositories

import com.ehedgehog.database.MarriageWithUsers
import com.ehedgehog.database.Marriages
import com.ehedgehog.database.Users
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

class MarriageRepository {

    fun marry(firstUserId: String, secondUserId: String) = transaction {
        Marriages.insert {
            it[Marriages.firstPartnerId] = minOf(firstUserId, secondUserId)
            it[Marriages.secondPartnerId] = maxOf(firstUserId, secondUserId)
            it[Marriages.marriedAt] = System.currentTimeMillis()
            it[Marriages.familyId] = UUID.randomUUID().toString()
        }
    }

    fun divorce(userId: String): Boolean = transaction {
        val familyId = Marriages.select(Marriages.familyId)
            .where { (Marriages.firstPartnerId eq userId) or (Marriages.secondPartnerId eq userId) }
            .singleOrNull()?.get(Marriages.familyId)

        familyId?.let { id ->
            Users.update({ Users.familyId eq id }) {
                it[Users.familyId] = null
            }
        }

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
        getMarriageWhere(
            (Marriages.firstPartnerId eq userId) or (Marriages.secondPartnerId eq userId)
        )
    }

    fun getMarriageByFamilyId(familyId: String): MarriageWithUsers? = transaction {
        getMarriageWhere(Marriages.familyId eq familyId)
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
                    marriedAt = it[Marriages.marriedAt],
                    familyId = it[Marriages.familyId]
                )
            }
    }

    private fun getMarriageWhere(predicate: Op<Boolean>): MarriageWithUsers? {
        val firstUser = Users.alias("user1")
        val secondUser = Users.alias("user2")

        return Marriages
            .join(firstUser, JoinType.INNER, Marriages.firstPartnerId, firstUser[Users.userId])
            .join(secondUser, JoinType.INNER, Marriages.secondPartnerId, secondUser[Users.userId])
            .selectAll()
            .where { predicate }
            .map {
                MarriageWithUsers(
                    firstUserId = it[Marriages.firstPartnerId],
                    firstUserName = it[firstUser[Users.name]],
                    secondUserId = it[Marriages.secondPartnerId],
                    secondUserName = it[secondUser[Users.name]],
                    marriedAt = it[Marriages.marriedAt],
                    familyId = it[Marriages.familyId]
                )
            }
            .singleOrNull()
    }

}