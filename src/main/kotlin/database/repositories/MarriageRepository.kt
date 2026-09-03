package com.ehedgehog.database.repositories

import com.ehedgehog.database.Marriages
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
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

}