package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseUserRepository
import com.ehedgehog.database.UserEntity
import dev.inmo.tgbotapi.types.chat.User

class ProfileRepository : BaseUserRepository() {

    fun getStoredUser(user: User): UserEntity {
        return getUserById(user.id.chatId.toString()) ?: run {
            val newUser = UserEntity(user.id.chatId.toString(), user.firstName, user.username?.username ?: "")
            updateUserEntry(newUser)
            newUser
        }
    }

}