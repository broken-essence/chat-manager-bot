package com.ehedgehog.screens.profile

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.base.getDescription
import com.ehedgehog.base.getRingStatus
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.types.chat.User

class ProfileManager : BaseUserManager() {

    private val repository = UserRepository()

    fun getProfileMessage(user: User): String {
        val userEntry = getStoredUserOrNew(user)
        val warnsVisible = userEntry.status > UserStatus.PLAYER

        return """|🪿 Пользователь *${handleReservedSymbols(user.firstName)}*
                |👤 Статус: ${userEntry.status.getDescription()}
                |💰 Ваш баланс: ${userEntry.balance} 💸
                |
                |🧻 Снятие варна: ${userEntry.unwarns}
                |💊 Активация иммунитета: ${userEntry.immunities}
                |Иммунитет: ${getImmunityStatus(userEntry)}
                |
                |💍 Кольцо: ${userEntry.getRingStatus()}
                |
                |${if (warnsVisible) "⚠️ Предупреждения: ${userEntry.adminWarns}\\/6" else ""}
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