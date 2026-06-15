package com.ehedgehog.screens.start

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.ScreenContext

class StartManager : BaseUserManager() {

    fun getStartMessage(context: ScreenContext): String {
        return "*Привет, ${createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())}\\!*\n\n" +
                "Это бот чата ɢᴏᴏðsᴇ ᴍᴀғɪᴀ, открывающий доступ к различным полезным бонусам и ряду других уникальных функций\\. " +
                "Всю нужную инфу можно найти по кнопкам внизу 👇"
    }

}