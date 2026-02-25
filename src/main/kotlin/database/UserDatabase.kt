package com.ehedgehog.database

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Users : Table("users") {
    val userId = varchar("user_id", 50).uniqueIndex()
    val name = varchar("name", 50)
    val count = integer("count").default(0)
    val status = integer("status").default(UserStatus.PLAYER.ordinal)
    val warns = integer("warns").default(0)
}

object UserDatabase {
    fun init() {
        /* local running */
        Database.connect("jdbc:sqlite:users.db", driver = "org.sqlite.JDBC")

        /* running with railway */
//        val host = System.getenv("PGHOST") ?: "localhost"
//        val port = System.getenv("PGPORT") ?: "5432"
//        val database = System.getenv("PGDATABASE") ?: "postgres"
//        val user = System.getenv("PGUSER") ?: "postgres"
//        val password = System.getenv("PGPASSWORD") ?: ""
//
//        val config = HikariConfig().apply {
//            this.jdbcUrl = "jdbc:postgresql://$host:$port/$database"
//            this.username = user
//            this.password = password
//            this.driverClassName = "org.postgresql.Driver"
//
//            maximumPoolSize = 5
//            minimumIdle = 1
//            connectionTimeout = 30_000
//            idleTimeout = 600_000
//            maxLifetime = 1_800_000
//
//            isAutoCommit = false
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
//        }
//
//        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(Users)
        }
    }

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
                it[count] = user.eventPointCount
                it[status] = user.status.ordinal
                it[warns] = user.adminWarns
            }
        }
    }
    
    fun getUserById(id: String): UserEntity? {
        return transaction { 
            Users.selectAll()
                .where(Users.userId eq id)
                .map { entry ->
                    UserEntity(
                        id = entry[Users.userId],
                        name = entry[Users.name],
                        eventPointCount = entry[Users.count],
                        status = UserStatus.fromInt(entry[Users.status]),
                        adminWarns = entry[Users.warns]
                    )
                }
                .singleOrNull()
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
}