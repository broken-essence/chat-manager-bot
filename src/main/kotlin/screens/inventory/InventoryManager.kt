package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.screens.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId

class InventoryManager(private val bot: TelegramBot) : BaseUserManager(bot) {

    private val repository = UserRepository()

    fun getInventoryMessage(userId: String): String {
        val userEntry = repository.getUserById(userId)

        return """
            *Ваш инвентарь:*
            
            🧻 *Снятие варна:* ${userEntry?.unwarns ?: 0}
            _📌 Предупреждение будет снято сразу после обработки запроса администратором\._
            
            💊 *Активация иммунитета:* ${userEntry?.immunities ?: 0}
            Иммунитет: не активен\.
            _📌 После активации обязательно необходимо добавить в ваш никнейм эмодзи «`🚩`», чтобы другие игроки видели наличие у вас активного иммунитета\._
        """.trimIndent()
    }

    suspend fun useUnwarn(context: ScreenContext) {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return

        if (userEntry.unwarns > 0) {
            //TODO: replace id with env of admin system chat
            val unwarnContext = ScreenContext(ChatId(RawChatId(-1002158551287)), context.user)
            updateUnwarns(
                ChatUser(context.chatId, userEntry, context.user),
                userEntry.unwarns - 1
            )
            ScreenRouter.openScreen(bot, unwarnContext, "request_unwarn")
            ScreenRouter.openScreen(bot, context, "inventory")
        }
    }

}