package com.ehedgehog.screens.inventory

import com.ehedgehog.base.BaseUserManager
import dev.inmo.tgbotapi.bot.TelegramBot

class InventoryManager(bot: TelegramBot) : BaseUserManager(bot) {

    private val repository = InventoryRepository()

    fun getInventoryMessage(userId: String): String {
        val userEntry = repository.getStoredUser(userId)

        return """
            *Ваш инвентарь:*
            
            🧻 *Снятие варна:* ${userEntry?.unwarns ?: 0}
            _📌 Предупреждение будет снято сразу после обработки запроса администратором\._
            
            💊 *Активация иммунитета:* ${userEntry?.immunities ?: 0}
            Иммунитет: не активен\.
            _📌 После активации обязательно необходимо добавить в ваш никнейм эмодзи «`🚩`», чтобы другие игроки видели наличие у вас активного иммунитета\._
        """.trimIndent()
    }

}