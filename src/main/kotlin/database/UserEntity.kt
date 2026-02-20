package com.ehedgehog.database

enum class UserStatus {PLAYER, ADMIN, SENIOR_ADMIN}

data class UserEntity(
    val id: String,
    val name: String,
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