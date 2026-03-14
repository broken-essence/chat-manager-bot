package com.ehedgehog.commands.event

import com.ehedgehog.base.BaseUserManager
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class EventManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = EventRepository()

    @OptIn(RiskFeature::class)
    suspend fun giveEventPoints(command: TextMessage, amount: Int = 1) {
        val repliedUser = command.replyTo?.from
        val markdownNameString = repliedUser?.let { createMarkdownLink(it.firstName, it.id.chatId.toString()) }

        if (repliedUser != null && amount >= 0) {
            if (isAdmin(command.chat.id, command.from!!.id)) {
                val eventPointCount = repository.getEventPointCountById(repliedUser.id.chatId.toString()) + amount
                repository.setEventPoints(repliedUser, eventPointCount)
                val amountString = createAmountString("начислен", "что-то", amount)
                bot.sendMessage(
                    command.chat.id,
                    """Пользователю $markdownNameString $amountString\!
                    |Всего печенюшек: $eventPointCount 🍪""".trimMargin(),
                    MarkdownV2
                )
            } else {
                bot.reply(command, "В админы метишь, бро?")
            }
        }
    }

    @OptIn(RiskFeature::class)
    suspend fun takeEventPoints(command: TextMessage, amount: Int = 1) {
        val repliedUser = command.replyTo?.from
        val markdownNameString = repliedUser?.let { createMarkdownLink(it.firstName, it.id.chatId.toString()) }

        if (repliedUser != null) {
            if (isAdmin(command.chat.id, command.from!!.id)) {
                val eventPointCount = repository.getEventPointCountById(repliedUser.id.chatId.toString())
                if (eventPointCount > 0) {
                    if (amount <= eventPointCount) {
                        val newCount = eventPointCount - amount
                        repository.setEventPoints(repliedUser, newCount)
                        val amountString = createAmountString("отобран", "что-то", amount)

                        bot.sendMessage(
                            command.chat.id,
                            """У пользователя $markdownNameString $amountString\!
                    |Всего печенюшек: $newCount 🍪""".trimMargin(),
                            MarkdownV2
                        )
                    } else {
                        bot.reply(command, "У данного пользователя нет столько печенюшек!")
                    }
                } else {
                    bot.reply(command, "У этого пользователя и так ничего нет!")
                }
            } else {
                bot.reply(command, "В админы метишь, бро?")
            }
        }
    }

    suspend fun getEventPointRating(command: TextMessage) {
        val eventPointList = repository.getTopByEventPoints()

        if (!eventPointList.isEmpty()) {
            val ratingString = eventPointList
                .joinToString("\n", "\uD83C\uDF6A *Рейтинг печенюшек:*\n\n") { "${it.index}\\. ${createMarkdownLink(it.name, it.id)} – ${it.eventPointCount} \uD83C\uDF6A" }
            bot.sendMessage(command.chat.id, ratingString, MarkdownV2)
        } else {
            bot.sendMessage(command.chat.id, "Список пуст.")
        }
    }

    @OptIn(RiskFeature::class)
    suspend fun getPersonalRating(command: TextMessage) {
        val user = command.from
        if (user != null) {
            val userMarkdown = createMarkdownLink(user.firstName, user.id.chatId.toString())
            val eventPointCount = repository.getEventPointCountById(user.id.chatId.toString())
            bot.reply(command, "\uD83C\uDF85 Пользователь ${userMarkdown}\n\uD83C\uDF81 *Ваш баланс:* $eventPointCount \uD83C\uDF6A", MarkdownV2)
        }
    }

    @OptIn(RiskFeature::class)
    suspend fun clearEventPoints(command: TextMessage) {
        if (isAdmin(command.chat.id, command.from!!.id)) {
            repository.clearEventPoints()
        }
    }

    suspend fun getCommands(command: TextMessage) {
        bot.sendMessage(
            command.chat.id,
            """*ᅠ   Команды бота:*
                |👮🏼 /cookie – подарить печенюшку \(reply\)
                |👮🏼 /take – забрать печенюшку \(reply\)
                |🪿 /rating – рейтинг печенюшек
                |🪿 /balance – посмотреть баланс печенюшек
                |🪿 /hint – список команд
            """.trimMargin(),
            MarkdownV2
        )
    }

    @OptIn(RiskFeature::class)
    suspend fun handleRPCommands(command: TextMessage) {
        val message = command.content.text
        if (message.startsWith("!")) {
            val senderUser = command.from
            val repliedUser = command.replyTo?.from
            val senderMarkdown = createMarkdownLink(senderUser?.firstName ?: "(null)", senderUser?.id?.chatId.toString())
            val repliedMarkdown = createMarkdownLink(repliedUser?.firstName ?: "(null)", repliedUser?.id?.chatId.toString())
            val splitMessage = message.split(" ")
            val resultMessage = when (splitMessage[0].lowercase()) {
                "!чай" -> {
                    if (repliedUser != null && senderUser?.id?.chatId.toString() != repliedUser.id.chatId.toString())
                        "$senderMarkdown заварил чашечку горячего чая для $repliedMarkdown \uD83C\uDF75\uD83E\uDD70"
                    else "$senderMarkdown заварил себе чашечку горячего чая \uD83C\uDF75"
                }
                "!подарок" -> {
                    if (repliedUser != null && senderUser?.id?.chatId.toString() != repliedUser.id.chatId.toString())
                        if (splitMessage.size > 1)
                            "\uD83C\uDF81 $senderMarkdown подарил $repliedMarkdown${message.removePrefix(splitMessage[0])} \uD83C\uDF81"
                        else "\uD83C\uDF81 $senderMarkdown подготовил подарок для $repliedMarkdown, но это сюрприз \uD83C\uDF81"
                    else "$senderMarkdown дарит сам себе подарки, печальное зрелище \uD83D\uDE22"
                }

                else -> return
            }

            bot.sendMessage(command.chat.id, resultMessage, MarkdownV2)
        }
    }

}