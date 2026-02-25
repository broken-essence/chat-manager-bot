package com.ehedgehog.commands.admin

import com.ehedgehog.commands.base.BaseUserRepository
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

class AdminRepository: BaseUserRepository() {

    fun setUserStatus(user: UserEntity, status: UserStatus) {
        updateUserEntry(UserEntity(user.id, user.name, user.eventPointCount, status))
    }

    fun updateWarns(user: UserEntity, warns: Int) {
        updateUserEntry(UserEntity(user.id, user.name, user.eventPointCount, user.status, 0, warns))
    }

}