package com.ehedgehog.database.repositories

import com.ehedgehog.database.DailyGoose
import com.ehedgehog.database.GooseResult
import com.ehedgehog.database.IdWithName
import com.ehedgehog.database.Users
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class DailyGooseRepository {

    private val userRepository = UserRepository()

    @OptIn(ExperimentalTime::class)
    fun chooseDailyGoose() = transaction {
        val todayMillis = Clock.System
            .todayIn(TimeZone.of("Europe/Kyiv"))
            .atStartOfDayIn(TimeZone.of("Europe/Kyiv"))
            .toEpochMilliseconds()

        val existing = DailyGoose
            .selectAll()
            .where { DailyGoose.dateMillis eq todayMillis }
            .singleOrNull()

        if (existing != null) {
            return@transaction GooseResult.Existing(userRepository.getUserById(existing[DailyGoose.userId])!!)
        }

        val randomUser = userRepository.getRandomUser() ?: return@transaction null
        DailyGoose.insert {
            it[dateMillis] = todayMillis
            it[userId] = randomUser.id
        }

        GooseResult.New(randomUser)
    }

    fun getLastGeese() = transaction {
        DailyGoose.join(Users, JoinType.INNER, DailyGoose.userId, Users.userId)
            .selectAll()
            .orderBy(DailyGoose.dateMillis, SortOrder.DESC)
            .limit(10)
            .map { IdWithName(it[DailyGoose.userId], it[Users.name]) }
    }

}