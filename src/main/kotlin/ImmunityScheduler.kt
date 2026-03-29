package com.ehedgehog

import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImmunityScheduler(
    private val bot: TelegramBot,
    private val repository: UserRepository,
    private val scope: CoroutineScope
) {

    fun scheduleExpirationNotification(userId: String, expiresAt: Long) {
        val delayMillis = expiresAt - System.currentTimeMillis()
        if (delayMillis <= 0) return

        scope.launch {
            delay(delayMillis)

            bot.sendMessage(
                ChatId(RawChatId(userId.toLong())),
                "\uD83D\uDD14 Срок действия вашего иммунитета истек! Пожалуйста, уберите эмодзи «\uD83D\uDEA9» из ника."
            )
        }
    }

    fun restoreNotifications() {
        val users = repository.getUsersWithActiveImmunity()

        users.forEach {
            scheduleExpirationNotification(it.id, it.immunityExpiresAt)
        }
    }

}