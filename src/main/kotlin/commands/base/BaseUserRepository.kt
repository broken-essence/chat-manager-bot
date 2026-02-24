package com.ehedgehog.commands.base

import com.ehedgehog.database.UserDatabase
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

open class BaseUserRepository {

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

    fun getUserStatusById(userId: String): UserStatus {
        return UserDatabase.getUserStatusById(userId)
    }

}