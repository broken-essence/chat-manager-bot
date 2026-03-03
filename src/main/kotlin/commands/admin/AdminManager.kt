package com.ehedgehog.commands.admin

import com.ehedgehog.commands.base.BaseUserManager
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.members.getChatMember
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.message.MarkdownV2
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class AdminManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = AdminRepository()

    @OptIn(RiskFeature::class)
    suspend fun changeUserStatus(command: TextMessage, statusValue: Int) {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
            if (repliedUser != null && statusValue in 0..<UserStatus.entries.size) {
                val user = repository.getUserById(repliedUser.id.chatId.toString())
                val markdownNameString = createMarkdownLink(repliedUser.firstName, repliedUser.id.chatId.toString())
                val status = UserStatus.fromInt(statusValue)

                repository.setUserStatus(
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
        onGiveCommand(command, content) { userEntity, reason ->
            addWarn(userEntity, reason, command.chat.id)
        }
    }

    suspend fun takeWarn(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            removeWarn(user, newCount, command.chat.id)
        }
    }

    suspend fun giveImmunity(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addImmunity(user, newCount, command.chat.id)
        }
    }

    suspend fun giveUnwarn(command: TextMessage, content: String) {
        onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addUnwarn(user, newCount, command.chat.id)
        }
    }

    @OptIn(RiskFeature::class)
    private suspend fun onGiveCommand(command: TextMessage, content: String, action: suspend (UserEntity?, String) -> Unit) {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
            val firstPart = content.split(" ")[0]
            if (firstPart.all { it in '0'..'9' } && firstPart.length > 1) {
                val userEntry = (repository.getUserById(firstPart) ?: run {
                    val user = bot.getChatMember(command.chat.id, UserId(RawChatId(firstPart.toLong()))).user
                    UserEntity(user.id.chatId.toString(), user.firstName, user.username?.username ?: "")
                })
                action(userEntry, content.removePrefix(firstPart).trim())
            } else if (firstPart.startsWith("@") && firstPart.length > 1) {
                val userEntry = repository.getUserByUsername(firstPart) ?: run {
                    bot.sendMessage(
                        command.chat.id,
                        "Пользователь $firstPart не найден.\nПопробуйте использовать id или ответить на его сообщение."
                    )
                    return
                }
                action(userEntry, content.removePrefix(firstPart).trim())
            } else if (repliedUser != null) {
                val userEntry = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    repliedUser.username?.username ?: ""
                )
                action(userEntry, content)
            }
        }
    }

    private suspend fun addWarn(userEntity: UserEntity?, reason: String, chatId: IdChatIdentifier) {
        userEntity?.let {
            if (it.status >= UserStatus.ADMIN) {
                val newCount = it.adminWarns + 1
                repository.updateWarns(it, newCount)

                bot.sendMessage(
                    chatId,
                    """
                    |Администратору ${createMarkdownLink(it.name, it.id)} выдано $newCount\/6 предупреждений\.
                    |${if (reason.isNotBlank()) "*Причина:* $reason" else ""}
                    """.trimMargin(),
                    MarkdownV2
                )
            }
        }
    }

    private suspend fun removeWarn(userEntity: UserEntity?, count: Int = 1, chatId: IdChatIdentifier) {
        userEntity?.let {
            if (it.status >= UserStatus.ADMIN) {
                if (count <= 0 || count > it.adminWarns) {
                    bot.sendMessage(chatId, "Неправильное количество предупреждений.")
                    return
                }

                val newCount = it.adminWarns - count
                val message = if (newCount == 0) "больше не имеет предупреждений\\." else "имеет $newCount\\/6 предупреждений\\."
                repository.updateWarns(it, newCount)
                bot.sendMessage(
                    chatId,
                    "Администратор ${createMarkdownLink(it.name, it.id)} $message",
                    MarkdownV2
                )
            }
        }
    }

    private suspend fun addImmunity(userEntity: UserEntity?, count: Int = 1, chatId: IdChatIdentifier) {
        userEntity?.let {
            val newCount = userEntity.immunities + count
            repository.updateImmunities(it, newCount)
            val actionString = createAmountString("подарен", "иммунитет", count)
            bot.sendMessage(
                chatId,
                "Пользователю ${createMarkdownLink(it.name, it.id)} ${actionString}\\.",
                MarkdownV2
            )
        }
    }

    private suspend fun addUnwarn(userEntity: UserEntity?, count: Int = 1, chatId: IdChatIdentifier) {
        userEntity?.let {
            val newCount = userEntity.unwarns + count
            repository.updateUnwarns(it, newCount)
            val actionString = createAmountString("подарен", "анварн", count)
            bot.sendMessage(
                chatId,
                "Пользователю ${createMarkdownLink(it.name, it.id)} ${actionString}\\.",
                MarkdownV2
            )
        }
    }

}