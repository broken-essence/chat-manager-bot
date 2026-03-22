package com.ehedgehog.database

import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.User

enum class UserStatus {
    PLAYER, ADMIN, SENIOR_ADMIN;

    companion object {
        fun fromInt(value: Int): UserStatus {
            return entries.first { it.ordinal == value }
        }
    }
}

data class UserEntity(
    val id: String,
    val name: String,
    val username: String = "",
    val eventPointCount: Int = 0,
    val status: UserStatus = UserStatus.PLAYER,
    val balance: Int = 0,
    val adminWarns: Int = 0,
    val unwarns: Int = 0,
    val immunities: Int = 0,
)

data class UserIndexed(
    val index: Int,
    val id: String,
    val name: String,
    val eventPointCount: Int
)

data class ChatUser(
    val chatId: IdChatIdentifier,
    val storedUser: UserEntity,
    val chatMember: User
)