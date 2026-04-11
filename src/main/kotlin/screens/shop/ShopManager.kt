package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.showPopup
import dev.inmo.tgbotapi.bot.TelegramBot

internal const val PRICE_UNWARN = 2
internal const val PRICE_IMMUNITY = 6

class ShopManager(private val bot: TelegramBot) : BaseUserManager(bot) {

    val repository = UserRepository()

    fun getShopMessage(userId: String): String {
        val user = repository.getUserById(userId)
        return """
            *Доступные товары:*
            
            *🧻 Снятие предупреждения*
            Позволяет отправить запрос на снятие одного предупреждения\.
            
            *💊 Иммунитет*
            Позволяет на 24 часа получить иммунитет от убийства и посещения активными ролями в *первые 2 игровые ночи*\.
            
            _📌 Приобретенные товары можно активировать в инвентаре в любое время\._
            
            💰 Ваш баланс: ${user?.balance ?: 0} 💸
        """.trimIndent()
    }

    suspend fun buyUnwarn(context: ScreenContext) {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return

        if (userEntry.balance >= PRICE_UNWARN) {
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    balance = userEntry.balance - PRICE_UNWARN,
                    unwarns = userEntry.unwarns + 1
                )
            )
            bot.showPopup(context, "Покупка совершена ✅")
            ScreenRouter.openScreen(bot, context, "shop")
        } else {
            bot.showPopup(context, "Недостаточно средств ❌")
        }
    }

    suspend fun buyImmunity(context: ScreenContext) {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return

        if (userEntry.balance >= PRICE_IMMUNITY) {
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    balance = userEntry.balance - PRICE_IMMUNITY,
                    immunities = userEntry.immunities + 1
                )
            )
            bot.showPopup(context, "Покупка совершена ✅")
            ScreenRouter.openScreen(bot, context, "shop")
        } else {
            bot.showPopup(context, "Недостаточно средств ❌")
        }
    }

}