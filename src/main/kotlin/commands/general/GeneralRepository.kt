package com.ehedgehog.commands.general

import com.ehedgehog.database.UserDatabase
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

class GeneralRepository {

    fun setUserStatus(user: UserEntity, status: UserStatus) {
        updateUserEntry(UserEntity(user.id, user.name, user.eventPointCount, status))
    }

    //TODO: move to base repository
    fun updateUserEntry(user: UserEntity) {
        try {
            UserDatabase.updateUserEntry(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getUserById(userId: String): UserEntity? {
        return UserDatabase.getUserById(userId)
    }

}