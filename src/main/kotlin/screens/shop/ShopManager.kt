package com.ehedgehog.screens.shop

import com.ehedgehog.base.BaseUserManager
import dev.inmo.tgbotapi.bot.TelegramBot

class ShopManager(bot: TelegramBot) : BaseUserManager(bot) {

    fun getShopMessage(): String {
        return """
            *Доступные товары:*
            
            *🧻 Снятие предупреждения*
            Позволяет отправить запрос на снятие одного предупреждения\.
            
            *💊 Иммунитет*
            Позволяет на 24 часа получить иммунитет от убийства и посещения активными ролями в *первые 2 игровые ночи*\.
            
            _📌 Приобретенные товары можно активировать в инвентаре в любое время\._
        """.trimIndent()
    }

}