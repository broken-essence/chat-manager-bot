package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseUserRepository
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity

class InventoryRepository : BaseUserRepository() {

    fun getStoredUser(id: String): UserEntity? {
        return getUserById(id)
    }

    fun useUnwarn(user: ChatUser) {
        updateUnwarns(user, user.storedUser.unwarns - 1)
    }

}