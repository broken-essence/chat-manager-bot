package com.ehedgehog.screens.profile

import com.ehedgehog.commands.base.BaseUserManager
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.chat.User

class ProfileManager(bot: TelegramBot) : BaseUserManager(bot) {

    private val repository = ProfileRepository()

    fun getProfileMessage(user: User): String {
        val userEntry = repository.getStoredUser(user)

        return """|🪿 Пользователь *${handleReservedSymbols(user.firstName)}*
                |👤 Статус: ${getStatusDescription(userEntry.status)}
                |💰 Ваш баланс: ${userEntry.balance} 💸
                |
                |🧻 Снятие варна: ${userEntry.unwarns}
                |💊 Активация иммунитета: ${userEntry.immunities}
                |Иммунитет: действует до 31\.07\.2048 17:41
                |
                |⚠️ Предупреждения: ${userEntry.adminWarns}\/6
                """.trimMargin()
    }

}