package com.ehedgehog.commands.event

import com.ehedgehog.database.DatabaseFactory
import com.ehedgehog.database.UserIndexed
import com.ehedgehog.database.UserEntity
import dev.inmo.tgbotapi.types.chat.User

//TODO: refactor to use UserRepository
class EventRepository {

    fun setEventPoints(user: User, count: Int) {
//        try {
//            DatabaseFactory.setEventPoints(UserEntity(user.id.chatId.toString(), user.firstName, eventPointCount = count))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    fun getEventPointCountById(userId: String): Int {
//        return DatabaseFactory.getEventPointCountById(userId)
        return 0
    }

    fun getTopByEventPoints(): List<UserIndexed> {
//        return DatabaseFactory.getTopByEventPoints()
//            .mapIndexed { index, user -> UserIndexed(index + 1, user.id, user.name, user.eventPointCount) }
        return emptyList()
    }

    fun clearEventPoints() {
//        DatabaseFactory.clearEventPoints()
    }

}