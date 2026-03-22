package com.ehedgehog.base

import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
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

    fun updateImmunities(user: ChatUser, immunCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                immunities = immunCount
            )
        )
    }

    fun updateUnwarns(user: ChatUser, unwarnCount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                unwarns = unwarnCount
            )
        )
    }

    fun updateBalance(user: ChatUser, amount: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                balance = amount
            )
        )
    }

    fun updateUserEntry(user: UserEntity) {
        try {
            repository.updateUserEntry(user)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}