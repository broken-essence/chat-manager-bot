package com.ehedgehog.screens.immunity_queue

import com.ehedgehog.AppContext
import com.ehedgehog.utils.ImmunityScheduler
import com.ehedgehog.base.*
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.JournalEvent
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ImmunityQueueManager(bot: TelegramBot) : BaseUserManager() {

    private val userRepository = UserRepository()
    private val immunityScheduler = ImmunityScheduler(bot, userRepository, CoroutineScope(Dispatchers.Default))

    fun getImmunityQueueMessage(): String {
        return "\uD83D\uDDD3 *Достигнуто максимальное количество пользователей с активным иммунитетом\\.*\n\nЖелаете встать в очередь?"
    }

    suspend fun confirmQueue(context: ScreenContext): ActionResult {
        val user = userRepository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)
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

            AppContext.journal.write(JournalEvent.Activation(user.id, user.name, "иммунитет"))
            immunityScheduler.scheduleImmunityActivatedNotification(user.id, startsAt)
            immunityScheduler.scheduleExpirationNotification(user.id, expiresAt)
            val newMessage = "*Вы заняли место в очереди\\.*\n\n✅ Ваш иммунитет активируется ${dateFromMillis(startsAt)} по МСК"
            return ActionResult.Success(newMessage)
        }

        return ActionResult.Failure(Reason.NotAvailable)
    }

}