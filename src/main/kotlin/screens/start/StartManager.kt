package com.ehedgehog.screens.start

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.ScreenContext

class StartManager : BaseUserManager() {

    fun getStartMessage(context: ScreenContext): String {
        return "*Привет, ${createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())}\\!*\n\n" +
                "Это бот чата ɢᴏᴏðsᴇ ᴍᴀғɪᴀ, в котором вы можете приобрести различные полезные бонусы и многое другое\\. " +
                "Всю нужную инфу можно найти по кнопкам внизу 👇"
    }

}