package com.ehedgehog.base

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

    fun getUserByUsername(username: String): UserEntity? {
        return UserDatabase.getUserByUsername(username)
    }

    fun getUserStatusById(userId: String): UserStatus {
        return UserDatabase.getUserStatusById(userId)
    }

}