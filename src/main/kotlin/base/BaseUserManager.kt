package com.ehedgehog.base

import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot

private const val TEST_IMMUNITY_DURATION = 60 * 1000
const val IMMUNITY_DURATION = /*24 * 60 * 60 * 1000*/ TEST_IMMUNITY_DURATION
private const val TEST_IMMUNITIES_COUNT_LIMIT = 1
const val IMMUNITIES_COUNT_LIMIT = /*5*/ TEST_IMMUNITIES_COUNT_LIMIT
private const val TEST_IMMUNITY_COOLDOWN = 2 * 60 * 1000
const val IMMUNITY_COOLDOWN = /*24 * 60 * 60 * 1000*/ TEST_IMMUNITY_COOLDOWN

abstract class BaseUserManager(bot: TelegramBot) : BaseManager(bot) {

    private val repository = UserRepository()

    fun getStatusDescription(status: UserStatus): String {
        return when (status) {
            UserStatus.PLAYER -> "Игрок"
            UserStatus.ADMIN -> "Администратор"
            UserStatus.SENIOR_ADMIN -> "Старший администратор"
        }
    }

    fun isSeniorAdminOrOwner(userId: String): Boolean {
        val status = repository.getUserStatusById(userId)
        return status == UserStatus.SENIOR_ADMIN || userId == System.getenv("BOT_OWNER_ID")
    }

    fun hasActiveImmunity(user: UserEntity): Boolean = user.immunityExpiresAt > System.currentTimeMillis()

    fun isInImmunityQueue(user: UserEntity): Boolean = user.immunityExpiresAt - IMMUNITY_DURATION > System.currentTimeMillis()

    fun hasImmunityCooldown(user: UserEntity): Boolean = System.currentTimeMillis() - user.immunityExpiresAt < IMMUNITY_COOLDOWN

    fun getImmunityStatus(user: UserEntity?): String = when {
        user == null -> "не активен"
        isInImmunityQueue(user) -> "в очереди"
        hasActiveImmunity(user) -> handleReservedSymbols("действует до ${dateFromMillis(user.immunityExpiresAt)} по МСК")
        hasImmunityCooldown(user) -> "восстанавливается"
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

    protected fun updateUserEntry(user: UserEntity) {
        try {
            repository.updateUserEntry(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}