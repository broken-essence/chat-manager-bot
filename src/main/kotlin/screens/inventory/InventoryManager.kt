package com.ehedgehog.screens.inventory

import com.ehedgehog.AppContext
import com.ehedgehog.utils.ImmunityScheduler
import com.ehedgehog.base.*
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.JournalEvent
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.repositories.UnwarnRequestRepository
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class InventoryManager(bot: TelegramBot) : BaseUserManager() {

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
            
            _📌 После активации необходимо добавить в ваш никнейм эмодзи «`🥦`», чтобы другие игроки видели наличие у вас активного иммунитета\._
            
            💍 *Кольцо:* ${userEntry?.getRingStatus()}
            
            _📌 Для того, чтобы сделать предложение, воспользуйтесь командой `/propose` в ответ на сообщение избранника._
        """.trimIndent()
    }

    suspend fun useUnwarn(context: ScreenContext): ActionResult {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)

        if (userEntry.unwarns > 0) {
            updateUnwarns(
                ChatUser(context.chatId, userEntry, context.user),
                userEntry.unwarns - 1
            )

            AppContext.journal.write(JournalEvent.Activation(userEntry.id, userEntry.name, "анварн"))
            val requestId = unwarnRequestRepository.createRequest(userEntry.id)
            return ActionResult.Success(requestId.toString())
        } else {
            return ActionResult.Failure(Reason.NotEnoughItems)
        }
    }

    suspend fun useImmunity(context: ScreenContext): ActionResult {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)
        val activeImmunities = userRepository.getUsersWithActiveImmunity()

        if (userEntry.immunities > 0 && !userEntry.hasActiveImmunity() && !userEntry.hasImmunityCooldown()) {
            if (activeImmunities.size >= AppContext.config.immunitiesCountLimit) {
                return ActionResult.Failure(Reason.LimitExceeded)
            }

            val expiresAt = System.currentTimeMillis() + AppContext.config.immunityDuration
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    immunities = userEntry.immunities - 1,
                    immunityExpiresAt = expiresAt
                )
            )

            AppContext.journal.write(JournalEvent.Activation(userEntry.id, userEntry.name, "иммунитет"))
            immunityScheduler.scheduleExpirationNotification(userEntry.id, expiresAt)
            return ActionResult.Success()
        }

        return ActionResult.Failure(Reason.NotAvailable)
    }

}