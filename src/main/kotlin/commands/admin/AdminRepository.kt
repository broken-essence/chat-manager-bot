package com.ehedgehog.commands.admin

import com.ehedgehog.commands.base.BaseUserRepository
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus

class AdminRepository: BaseUserRepository() {

    fun setUserStatus(user: UserEntity, status: UserStatus) {
        updateUserEntry(UserEntity(user.id, user.name, user.username, user.eventPointCount, status, user.balance, user.adminWarns, user.unwarns, user.immunities))
    }

    fun updateWarns(user: UserEntity, warns: Int) {
        updateUserEntry(UserEntity(user.id, user.name, user.username, user.eventPointCount, user.status, user.balance, warns, user.unwarns, user.immunities))
    }

    fun updateImmunities(user: UserEntity, immunCount: Int) {
        updateUserEntry(UserEntity(user.id, user.name, user.username, user.eventPointCount, user.status, user.balance, user.adminWarns, user.unwarns, immunCount))
    }

    fun updateUnwarns(user: UserEntity, unwarnCount: Int) {
        updateUserEntry(UserEntity(user.id, user.name, user.username, user.eventPointCount, user.status, user.balance, user.adminWarns, unwarnCount, user.immunities))
    }

    fun updateBalance(user: UserEntity, amount: Int) {
        updateUserEntry(UserEntity(user.id, user.name, user.username, user.eventPointCount, user.status, amount, user.adminWarns, user.unwarns, user.immunities))
    }

}