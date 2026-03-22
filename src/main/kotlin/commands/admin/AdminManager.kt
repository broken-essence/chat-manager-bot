package com.ehedgehog.commands.admin

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class AdminManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = UserRepository()

    @OptIn(RiskFeature::class)
    suspend fun changeUserStatus(command: TextMessage, statusValue: Int) {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
            if (repliedUser != null && statusValue in 0..<UserStatus.entries.size) {
                val user = repository.getUserById(repliedUser.id.chatId.toString())
                val markdownNameString = createMarkdownLink(repliedUser.firstName, repliedUser.id.chatId.toString())
                val status = UserStatus.fromInt(statusValue)

                setUserStatus(
                    user ?: UserEntity(repliedUser.id.chatId.toString(), repliedUser.firstName, repliedUser.username?.username ?: ""),
                    status
                )

                bot.sendMessage(
                    command.chat.id,
                    "$markdownNameString теперь *${getStatusDescription(status)}*",
                    MarkdownV2
                )
            }
        }
    }

    @OptIn(RiskFeature::class)
    suspend fun giveWarn(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, reason ->
            addWarn(user, reason)
        }
    }

    suspend fun takeWarn(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            removeWarn(user, newCount)
        }
    }

    suspend fun giveImmunity(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addImmunity(user, newCount)
        }
    }

    suspend fun giveUnwarn(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addUnwarn(user, newCount)
        }
    }

    suspend fun giveBalance(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, amount ->
            val newAmount = if (amount.isNotBlank()) amount.trim().toInt() else 1
            addBalance(user, newAmount)
        }
    }

    @OptIn(RiskFeature::class)
    private suspend fun onGiveCommand(command: TextMessage, content: String, action: suspend (ChatUser, String) -> Unit) {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
            val firstPart = content.split(" ")[0]
            if (firstPart.all { it in '0'..'9' } && firstPart.length >= 9) {
                val chatMember = bot.getChatUserById(command.chat.id, firstPart.toLong())
                val userEntry = repository.getUserById(firstPart) ?: UserEntity(
                    chatMember.id.chatId.toString(),
                    chatMember.firstName,
                    chatMember.username?.username ?: ""
                )
                action(ChatUser(command.chat.id,userEntry, chatMember), content.removePrefix(firstPart).trim())
            } else if (firstPart.startsWith("@") && firstPart.length > 1) {
                val userEntry = repository.getUserByUsername(firstPart) ?: run {
                    bot.sendMessage(
                        command.chat.id,
                        "Пользователь $firstPart не найден.\nПопробуйте использовать id или ответить на его сообщение."
                    )
                    return
                }
                val chatMember = bot.getChatUserById(command.chat.id, userEntry.id.toLong())
                action(ChatUser(command.chat.id, userEntry, chatMember), content.removePrefix(firstPart).trim())
            } else if (repliedUser != null) {
                val userEntry = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    repliedUser.username?.username ?: ""
                )
                action(ChatUser(command.chat.id, userEntry, repliedUser), content)
            }
        }
    }

    private suspend fun addWarn(chatUser: ChatUser, reason: String) {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            val newCount = chatUser.storedUser.adminWarns + 1
            updateWarns(chatUser, newCount)

            bot.sendMessage(
                chatUser.chatId,
                """
                    |Администратору ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} выдано $newCount\/6 предупреждений\.
                    |${if (reason.isNotBlank()) "*Причина:* $reason" else ""}
                    """.trimMargin(),
                MarkdownV2
            )
        }
    }

    private suspend fun removeWarn(chatUser: ChatUser, count: Int = 1) {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            if (count <= 0 || count > chatUser.storedUser.adminWarns) {
                bot.sendMessage(chatUser.chatId, "Неправильное количество предупреждений.")
                return
            }

            val newCount = chatUser.storedUser.adminWarns - count
            val message = if (newCount == 0) "больше не имеет предупреждений\\." else "имеет $newCount\\/6 предупреждений\\."
            updateWarns(chatUser, newCount)
            bot.sendMessage(
                chatUser.chatId,
                "Администратор ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} $message",
                MarkdownV2
            )
        }
    }

    private suspend fun addImmunity(chatUser: ChatUser, count: Int = 1) {
        val newCount = chatUser.storedUser.immunities + count
        updateImmunities(chatUser, newCount)
        val actionString = createAmountString("подарен", "иммунитет", count)
        bot.sendMessage(
            chatUser.chatId,
            "Пользователю ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} ${actionString}\\.",
            MarkdownV2
        )
    }

    private suspend fun addUnwarn(chatUser: ChatUser, count: Int = 1) {
        val newCount = chatUser.storedUser.unwarns + count
        updateUnwarns(chatUser, newCount)
        val actionString = createAmountString("подарен", "анварн", count)
        bot.sendMessage(
            chatUser.chatId,
            "Пользователю ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} ${actionString}\\.",
            MarkdownV2
        )
    }

    private suspend fun addBalance(chatUser: ChatUser, amount: Int = 0) {
        val newAmount = chatUser.storedUser.balance + amount
        updateBalance(chatUser, newAmount)

        bot.sendMessage(
            chatUser.chatId,
            "Пользователю ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} выдано $amount \uD83D\uDCB8",
            MarkdownV2
        )
    }

    private fun setUserStatus(user: UserEntity, status: UserStatus) {
        updateUserEntry(user.copy(status = status))
    }

    private fun updateWarns(user: ChatUser, warns: Int) {
        updateUserEntry(
            user.storedUser.copy(
                name = user.chatMember.firstName,
                username = user.chatMember.username?.username ?: "",
                adminWarns = warns
            )
        )
    }

}