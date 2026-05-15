package com.ehedgehog.utils

import com.ehedgehog.base.BaseManager
import com.ehedgehog.data.JournalEvent
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.message.MarkdownV2

class UserActionsJournal private constructor(private val bot: TelegramBot) : BaseManager() {

    companion object {
        fun create(bot: TelegramBot): UserActionsJournal = UserActionsJournal(bot)
    }

    suspend fun write(event: JournalEvent) {
        val eventString = format(event)
        val channelId = ChatId(RawChatId(System.getenv("JOURNAL_CHANNEL_ID").toLong()))
        bot.sendMessage(channelId, eventString, MarkdownV2)
    }

    private fun format(event: JournalEvent): String = when (event) {
        is JournalEvent.Purchase -> """
            🛒 \#ПОКУПКА \#${event.item.uppercase()}
            |*Кто:* ${createMarkdownLink(event.name, event.userId)} \[`${event.userId}`\]
            |*Покупка:* ${event.item}
            |*Дата:* ${dateFromMillis(System.currentTimeMillis())}
            |\#id${event.userId}
        """.trimMargin()

        is JournalEvent.Activation -> """
            |✅ \#АКТИВАЦИЯ \#${event.item.uppercase()}
            |*Кто:* ${createMarkdownLink(event.name, event.userId)} \[`${event.userId}`\]
            |*Активировано:* ${event.item}
            |*Дата:* ${dateFromMillis(System.currentTimeMillis())}
            |\#id${event.userId}
        """.trimMargin()

        is JournalEvent.NewUser -> """
            |🚗 \#НОВЫЙ_ПОЛЬЗОВАТЕЛЬ
            |*Кто:* ${createMarkdownLink(event.name, event.userId)} \[`${event.userId}`\]
            |*Дата:* ${dateFromMillis(System.currentTimeMillis())}
            |\#id${event.userId}
        """.trimMargin()

        is JournalEvent.WarnsUpdate -> """
            |⚠️ \#ОБНОВЛЕНИЕ_ПРЕДУПРЕЖДЕНИЙ
            |*Кто:* ${createMarkdownLink(event.fromName, event.fromId)} \[`${event.fromId}`\]
            |*Кому:* ${createMarkdownLink(event.name, event.userId)} \[`${event.userId}`\]
            |*Новое количество:* ${event.newCount}\/6
            |*Причина:* ${event.reason ?: "снятие"}
            |*Дата:* ${dateFromMillis(System.currentTimeMillis())}
            |\#id${event.userId}
        """.trimMargin()

        is JournalEvent.ItemGiving -> """
            |📦 \#ВЫДАНО \#${event.item.uppercase()}
            |*Кто:* ${createMarkdownLink(event.fromName, event.fromId)} \[`${event.fromId}`\]
            |*Кому:* ${createMarkdownLink(event.name, event.userId)} \[`${event.userId}`\]
            |*Выдано: * ${event.item} ${event.count} шт\.
            |*Дата:* ${dateFromMillis(System.currentTimeMillis())}
            |\#id${event.userId}
        """.trimMargin()
    }

}