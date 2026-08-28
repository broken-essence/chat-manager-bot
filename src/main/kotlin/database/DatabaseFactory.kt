package com.ehedgehog.database

import com.ehedgehog.AppContext
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun init() {
        AppContext.config.connectDatabase()

        transaction {
            SchemaUtils.create(Users, UnwarnRequests, Settings, Marriages)
        }
    }

}