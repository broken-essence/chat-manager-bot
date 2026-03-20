package com.ehedgehog.base

import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserDatabase
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

open class BaseUserRepository {

    fun getUserById(userId: String): UserEntity? {
        return UserDatabase.getUserById(userId)
    }

    fun getUserByUsername(username: String): UserEntity? {
        return UserDatabase.getUserByUsername(username)
    }

    fun getUserStatusById(userId: String): UserStatus {
        return UserDatabase.getUserStatusById(userId)
    }

    fun updateUserEntry(user: UserEntity) {
        try {
            UserDatabase.updateUserEntry(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateImmunities(user: ChatUser, immunCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                immunities = immunCount
            )
        )
    }

    fun updateUnwarns(user: ChatUser, unwarnCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                unwarns = unwarnCount
            )
        )
    }

    fun updateBalance(user: ChatUser, amount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                balance = amount
            )
        )
    }

}