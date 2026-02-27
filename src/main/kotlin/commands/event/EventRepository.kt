package com.ehedgehog.commands.event

import com.ehedgehog.database.UserDatabase
import com.ehedgehog.database.UserIndexed
import com.ehedgehog.database.UserEntity
import dev.inmo.tgbotapi.types.chat.User

class EventRepository {

    fun setEventPoints(user: User, count: Int) {
        try {
            UserDatabase.setEventPoints(UserEntity(user.id.chatId.toString(), user.firstName, eventPointCount = count))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getEventPointCountById(userId: String): Int {
        return UserDatabase.getEventPointCountById(userId)
    }

    fun getTopByEventPoints(): List<UserIndexed> {
        return UserDatabase.getTopByEventPoints()
            .mapIndexed { index, user -> UserIndexed(index + 1, user.id, user.name, user.eventPointCount) }
    }

    fun clearEventPoints() {
        UserDatabase.clearEventPoints()
    }

}