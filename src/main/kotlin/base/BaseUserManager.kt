package com.ehedgehog.base

import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.inventory.IMMUNITY_DURATION
import dev.inmo.tgbotapi.bot.TelegramBot

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

    fun getImmunityStatus(user: UserEntity?): String =
        if (user != null && hasActiveImmunity(user)) {
            if (user.immunityExpiresAt - IMMUNITY_DURATION > System.currentTimeMillis())
                "в очереди"
            else handleReservedSymbols("действует до ${dateFromMillis(user.immunityExpiresAt)} по МСК")
        } else "не активен"

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