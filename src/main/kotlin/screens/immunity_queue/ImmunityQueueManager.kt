package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.ImmunityScheduler
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.base.IMMUNITIES_COUNT_LIMIT
import com.ehedgehog.base.IMMUNITY_DURATION
import com.ehedgehog.base.hasActiveImmunity
import com.ehedgehog.base.hasImmunityCooldown
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ScreenContext
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.deleteMessage
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.types.message.MarkdownV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ImmunityQueueManager(private val bot: TelegramBot) : BaseUserManager(bot) {

    private val userRepository = UserRepository()
    private val immunityScheduler = ImmunityScheduler(bot, userRepository, CoroutineScope(Dispatchers.Default))

    fun getImmunityQueueMessage(): String {
        return "\uD83D\uDDD3 *Достигнуто максимальное количество пользователей с активным иммунитетом\\.*\n\nЖелаете встать в очередь?"
    }

    suspend fun confirmQueue(context: ScreenContext) {
        val user = userRepository.getUserById(context.user.id.chatId.toString()) ?: return
        val immunities = userRepository.getUsersWithActiveImmunity()

        if (user.immunities > 0 && !user.hasActiveImmunity() && !user.hasImmunityCooldown()) {
            val startsAt = if (immunities.size >= IMMUNITIES_COUNT_LIMIT)
                immunities[immunities.size - IMMUNITIES_COUNT_LIMIT].immunityExpiresAt
            else System.currentTimeMillis()
            val expiresAt = startsAt + IMMUNITY_DURATION

            updateUserEntry(
                user.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    immunities = user.immunities - 1,
                    immunityExpiresAt = expiresAt
                )
            )

            immunityScheduler.scheduleImmunityActivatedNotification(user.id, startsAt)
            immunityScheduler.scheduleExpirationNotification(user.id, expiresAt)
            val newMessage = "*Вы заняли место в очереди\\.*\n\n✅ Ваш иммунитет активируется ${dateFromMillis(startsAt)} по МСК"
            context.messageId?.let {
                bot.editMessageText(context.chatId, it, newMessage, MarkdownV2)
            }
        } else {
            context.messageId?.let { bot.delete(context.chatId, it) }
        }
    }

    suspend fun declineQueue(context: ScreenContext) {
        context.messageId?.let { bot.deleteMessage(context.chatId, it) }
    }

}