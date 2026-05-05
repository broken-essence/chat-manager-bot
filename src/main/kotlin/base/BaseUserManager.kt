package com.ehedgehog.base

import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository

private const val TEST_IMMUNITY_DURATION = 60 * 1000
const val IMMUNITY_DURATION = /*24 * 60 * 60 * 1000*/ TEST_IMMUNITY_DURATION
private const val TEST_IMMUNITIES_COUNT_LIMIT = 1
const val IMMUNITIES_COUNT_LIMIT = /*5*/ TEST_IMMUNITIES_COUNT_LIMIT
private const val TEST_IMMUNITY_COOLDOWN = 2 * 60 * 1000
const val IMMUNITY_COOLDOWN = /*24 * 60 * 60 * 1000*/ TEST_IMMUNITY_COOLDOWN

abstract class BaseUserManager : BaseManager() {

    private val repository = UserRepository()

    fun getStatusDescription(status: UserStatus): String {
        return when (status) {
            UserStatus.PLAYER -> "Игрок"
            UserStatus.ADMIN -> "Администратор"
            UserStatus.SENIOR_ADMIN -> "Старший администратор"
        }
    }

    fun isAdmin(userId: String): Boolean {
        val status = repository.getUserStatusById(userId)
        return status >= UserStatus.ADMIN || userId == System.getenv("BOT_OWNER_ID")
    }

    fun isSeniorAdmin(userId: String): Boolean {
        val status = repository.getUserStatusById(userId)
        return status == UserStatus.SENIOR_ADMIN || userId == System.getenv("BOT_OWNER_ID")
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
    get() = immunityExpiresAt - IMMUNITY_DURATION

fun UserEntity.hasActiveImmunity(): Boolean = immunityExpiresAt > System.currentTimeMillis()

fun UserEntity.isInImmunityQueue(): Boolean = immunityStartsAt > System.currentTimeMillis()

fun UserEntity.hasImmunityCooldown(): Boolean = System.currentTimeMillis() - immunityExpiresAt < IMMUNITY_COOLDOWN