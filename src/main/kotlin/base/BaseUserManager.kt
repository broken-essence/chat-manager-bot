package com.ehedgehog.base

import com.ehedgehog.database.UserStatus
import dev.inmo.tgbotapi.bot.TelegramBot

abstract class BaseUserManager(bot: TelegramBot) : BaseManager(bot) {

    private val repository = BaseUserRepository()

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

}