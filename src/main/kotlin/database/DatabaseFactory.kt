package com.ehedgehog.database

import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

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
            SchemaUtils.create(Users, UnwarnRequests)
        }
    }

}