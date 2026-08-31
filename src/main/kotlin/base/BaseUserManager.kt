package com.ehedgehog.base

import com.ehedgehog.AppContext
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository

abstract class BaseUserManager : BaseManager() {

    private val repository = UserRepository()

    fun isAdmin(userId: String): Boolean {
        val status = repository.getUserStatusById(userId)
        return status >= UserStatus.ADMIN || userId == AppContext.config.botOwnerId
    }

    fun isSeniorAdmin(userId: String): Boolean {
        val status = repository.getUserStatusById(userId)
        return status == UserStatus.SENIOR_ADMIN || userId == AppContext.config.botOwnerId
    }

    fun getImmunityStatus(user: UserEntity?): String = when {
        user == null -> "не активен"
        user.isInImmunityQueue() -> "в очереди"
        user.hasActiveImmunity() -> handleReservedSymbols("действует до ${dateFromMillis(user.immunityExpiresAt)} по МСК")
        user.hasImmunityCooldown() -> "восстанавливается"
        else -> "не активен"
    }

    protected fun updateImmunities(user: ChatUser, immunityCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                immunities = immunityCount
            )
        )
    }

    protected fun updateUnwarns(user: ChatUser, unwarnCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                unwarns = unwarnCount
            )
        )
    }

    protected fun updateBalance(user: ChatUser, amount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                balance = amount
            )
        )
    }

    protected fun updateEventPoints(user: ChatUser, count: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                eventPoints = count
            )
        )
    }

    protected fun updateUserEntry(user: UserEntity) {
        try {
            repository.updateUserEntry(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

val UserEntity.immunityStartsAt
    get() = immunityExpiresAt - AppContext.config.immunityDuration

fun UserEntity.hasActiveImmunity(): Boolean = immunityExpiresAt > System.currentTimeMillis()

fun UserEntity.isInImmunityQueue(): Boolean = immunityStartsAt > System.currentTimeMillis()

fun UserEntity.hasImmunityCooldown(): Boolean = System.currentTimeMillis() - immunityExpiresAt < AppContext.config.immunityCooldown

fun UserEntity.getRingStatus(): String = if (hasRing) "приобретено" else "отсутствует"

fun UserStatus.getDescription(): String = when (this) {
    UserStatus.PLAYER -> "Игрок"
    UserStatus.ADMIN -> "Администратор"
    UserStatus.SENIOR_ADMIN -> "Старший администратор"
}