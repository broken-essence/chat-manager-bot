package com.ehedgehog.database.repositories

import com.ehedgehog.database.UnwarnRequest
import com.ehedgehog.database.UnwarnRequests
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UnwarnRequestRepository {

    fun createRequest(userId: String): Int = transaction {
        UnwarnRequests.insertAndGetId {
            it[UnwarnRequests.userId] = userId
            it[UnwarnRequests.createdAt] = System.currentTimeMillis()
        }
    }.value

    fun getRequest(id: Int): UnwarnRequest? = transaction {
        UnwarnRequests.selectAll()
            .where{ UnwarnRequests.id eq id }
            .map {
                UnwarnRequest(
                    id = it[UnwarnRequests.id].value,
                    userId = it[UnwarnRequests.userId],
                    createdAt = it[UnwarnRequests.createdAt]
                )
            }
            .singleOrNull()
    }

    fun deleteRequest(id: Int) = transaction {
        UnwarnRequests.deleteWhere { UnwarnRequests.id eq id }
    }

}