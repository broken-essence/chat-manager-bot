package com.ehedgehog.commands.admin

import com.ehedgehog.base.BaseUserRepository
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

class AdminRepository: BaseUserRepository() {

    fun setUserStatus(user: UserEntity, status: UserStatus) {
        updateUserEntry(user.copy(status = status))
    }

    fun updateWarns(user: ChatUser, warns: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                adminWarns = warns
            )
        )
    }

}