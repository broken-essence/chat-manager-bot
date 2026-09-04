package com.ehedgehog.screens.shop

import com.ehedgehog.AppContext
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.JournalEvent
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.repositories.UserRepository

internal const val PRICE_UNWARN = 3
internal const val PRICE_IMMUNITY = 8
internal const val PRICE_RING = 5

class ShopManager : BaseUserManager() {

    val repository = UserRepository()

    fun getShopMessage(userId: String): String {
        val user = repository.getUserById(userId)
        return """
            *Доступные товары:*
            
            *🧻 Снятие предупреждения*
            Позволяет отправить запрос на снятие одного предупреждения\.
            
            *💊 Иммунитет*
            Позволяет на 24 часа получить иммунитет от убийства и посещения активными ролями в *первые 2 игровые ночи*\.
            
            *💍 Кольцо*
            Позволяет сделать предложение о вступлении в брак выбранному пользователю с помощью команды `/propose`\.
            
            _📌 Приобретенные товары можно активировать в инвентаре в любое время\._
            
            💰 Ваш баланс: ${user?.balance ?: 0} 💸
        """.trimIndent()
    }

    suspend fun buyUnwarn(context: ScreenContext): ActionResult {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)

        if (userEntry.balance >= PRICE_UNWARN) {
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    balance = userEntry.balance - PRICE_UNWARN,
                    unwarns = userEntry.unwarns + 1
                )
            )
            AppContext.journal.write(JournalEvent.Purchase(userEntry.id, userEntry.name, "анварн"))

            return ActionResult.Success()
        }

        return ActionResult.Failure(Reason.NotEnoughBalance)
    }

    suspend fun buyImmunity(context: ScreenContext): ActionResult {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)

        if (userEntry.balance >= PRICE_IMMUNITY) {
            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    balance = userEntry.balance - PRICE_IMMUNITY,
                    immunities = userEntry.immunities + 1
                )
            )
            AppContext.journal.write(JournalEvent.Purchase(userEntry.id, userEntry.name, "иммунитет"))

            return ActionResult.Success()
        }

        return ActionResult.Failure(Reason.NotEnoughBalance)
    }

    suspend fun buyRing(context: ScreenContext): ActionResult {
        val userEntry = repository.getUserById(context.user.id.chatId.toString()) ?: return ActionResult.Failure(Reason.UserNotFound)

        if (userEntry.balance >= PRICE_RING) {
            if (userEntry.hasRing) return ActionResult.Failure(Reason.LimitExceeded)

            updateUserEntry(
                userEntry.copy(
                    name = context.user.firstName,
                    username = context.user.username?.username ?: "",
                    balance = userEntry.balance - PRICE_RING,
                    hasRing = true
                )
            )
            AppContext.journal.write(JournalEvent.Purchase(userEntry.id, userEntry.name, "кольцо"))

            return ActionResult.Success()
        }

        return ActionResult.Failure(Reason.NotEnoughBalance)
    }

}