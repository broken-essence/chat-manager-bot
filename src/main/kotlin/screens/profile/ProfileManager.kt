package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.chat.User

class ProfileManager(bot: TelegramBot) : BaseUserManager(bot) {

    private val repository = UserRepository()

    fun getProfileMessage(user: User): String {
        val userEntry = getStoredUserOrNew(user)

        return """|🪿 Пользователь *${handleReservedSymbols(user.firstName)}*
                |👤 Статус: ${getStatusDescription(userEntry.status)}
                |💰 Ваш баланс: ${userEntry.balance} 💸
                |
                |🧻 Снятие варна: ${userEntry.unwarns}
                |💊 Активация иммунитета: ${userEntry.immunities}
                |Иммунитет: ${getImmunityStatus(userEntry)}
                |
                |⚠️ Предупреждения: ${userEntry.adminWarns}\/6
                """.trimMargin()
    }

    private fun getStoredUserOrNew(user: User): UserEntity {
        return repository.getUserById(user.id.chatId.toString()) ?: run {
            val newUser = UserEntity(user.id.chatId.toString(), user.firstName, user.username?.username ?: "")
            updateUserEntry(newUser)
            newUser
        }
    }

}