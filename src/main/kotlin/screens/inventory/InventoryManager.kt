package com.ehedgehog.screens.inventory

import com.ehedgehog.ImmunityScheduler
import com.ehedgehog.base.*
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.repositories.UnwarnRequestRepository
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class InventoryManager(bot: TelegramBot) : BaseUserManager(bot) {

    private val userRepository = UserRepository()
    private val unwarnRequestRepository = UnwarnRequestRepository()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val immunityScheduler = ImmunityScheduler(bot, userRepository, coroutineScope)

    fun getInventoryMessage(userId: String): String {
        val userEntry = userRepository.getUserById(userId)

        return """
            *Ваш инвентарь:*
            
            🧻 *Снятие варна:* ${userEntry?.unwarns ?: 0}
            _📌 Предупреждение будет снято сразу после обработки запроса администратором\._
            
            💊 *Активация иммунитета:* ${userEntry?.immunities ?: 0}
            Иммунитет: ${getImmunityStatus(userEntry)}\.
            _📌 После активации необходимо добавить в ваш никнейм эмодзи «`🚩`», чтобы другие игроки видели наличие у вас активного иммунитета\._
        """.trimIndent()
    }

    fun useUnwarn(context: ScreenContext): ActionResult {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)

        if (userEntry.unwarns > 0) {
            updateUnwarns(
                ChatUser(context.chatId, userEntry, context.user),
                userEntry.unwarns - 1
            )
            val requestId = unwarnRequestRepository.createRequest(userEntry.id)
            return ActionResult.Success(requestId.toString())
        } else {
            return ActionResult.Failure(Reason.NotEnoughItems)
        }
    }

    fun useImmunity(context: ScreenContext): ActionResult {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)
        val activeImmunities = userRepository.getUsersWithActiveImmunity()

        if (userEntry.immunities > 0 && !userEntry.hasActiveImmunity() && !userEntry.hasImmunityCooldown()) {
            if (activeImmunities.size >= IMMUNITIES_COUNT_LIMIT) {
                return ActionResult.Failure(Reason.LimitExceeded)
            }

            val expiresAt = System.currentTimeMillis() + IMMUNITY_DURATION
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    immunities = userEntry.immunities - 1,
                    immunityExpiresAt = expiresAt
                )
            )

            immunityScheduler.scheduleExpirationNotification(userEntry.id, expiresAt)
            return ActionResult.Success()
        }

        return ActionResult.Failure(Reason.NotAvailable)
    }

}