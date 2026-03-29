package com.ehedgehog.screens.inventory

import com.ehedgehog.ImmunityScheduler
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.repositories.UnwarnRequestRepository
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private const val IMMUNITY_DURATION = 24 * 60 * 60 * 1000
private const val TEST_IMMUNITY_DURATION = 60 * 1000

class InventoryManager(private val bot: TelegramBot) : BaseUserManager(bot) {

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

    suspend fun useUnwarn(context: ScreenContext) {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return

        if (userEntry.unwarns > 0) {
            //TODO: replace id with env of admin system chat
            val unwarnContext = ScreenContext(ChatId(RawChatId(-1002158551287)), context.user)
            updateUnwarns(
                ChatUser(context.chatId, userEntry, context.user),
                userEntry.unwarns - 1
            )
            val requestId = unwarnRequestRepository.createRequest(userEntry.id)
            ScreenRouter.openScreen(bot, unwarnContext, "request_unwarn", requestId.toString())
            ScreenRouter.openScreen(bot, context, "inventory")
        }
    }

    suspend fun useImmunity(context: ScreenContext) {
        val userEntry = userRepository.getUserById(context.user.id.chatId.toString()) ?: return

        if (userEntry.immunities > 0 && !hasActiveImmunity(userEntry)) {
            val expiresAt = System.currentTimeMillis() + TEST_IMMUNITY_DURATION
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    immunities = userEntry.immunities - 1,
                    immunityExpiresAt = expiresAt
                )
            )

            immunityScheduler.scheduleExpirationNotification(userEntry.id, expiresAt)
            ScreenRouter.openScreen(bot, context, "inventory")
        }
    }

}