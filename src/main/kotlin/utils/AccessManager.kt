package com.ehedgehog.utils

object AccessManager {

    private val blockedUsers = mutableSetOf<String>()

    fun init(blockedIds: Collection<String>) {
        blockedUsers.clear()
        blockedUsers.addAll(blockedIds)
    }

    fun isBlocked(id: String): Boolean = id in blockedUsers

    fun blockUser(id: String) {
        blockedUsers.add(id)
    }

    fun unblockUser(id: String) {
        blockedUsers.remove(id)
    }

}