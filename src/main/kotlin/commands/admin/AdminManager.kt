package com.ehedgehog.commands.admin

import com.ehedgehog.AppContext
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.base.getDescription
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.JournalEvent
import com.ehedgehog.data.Reason
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import com.ehedgehog.utils.PluralsUtil
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

@OptIn(RiskFeature::class)
class AdminManager(private val bot: TelegramBot): BaseUserManager() {

    private val repository = UserRepository()

    suspend fun changeUserStatus(command: TextMessage, statusValue: Int): CommandResult {
        val repliedUser = command.replyTo?.from
        val fromUser = command.from ?: return CommandResult.Failure(Reason.UnexpectedError)

        if (isSeniorAdmin(fromUser.id.chatId.toString())) {
            if (repliedUser != null && statusValue in 0..<UserStatus.entries.size) {
                val userId = repliedUser.id.chatId.toString()
                val user = repository.getUserById(userId)
                val markdownNameString = createMarkdownLink(repliedUser.firstName, userId)
                val status = UserStatus.fromInt(statusValue)

                setUserStatus(
                    user ?: UserEntity(userId, repliedUser.firstName, repliedUser.username?.username ?: ""),
                    status
                )

                AppContext.journal.write(JournalEvent.StatusChanged(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    fromUser.id.chatId.toString(),
                    fromUser.firstName,
                    status
                ))
                val message = "$markdownNameString теперь *${status.getDescription()}*"
                return CommandResult.Success(message, userId)
            }

            return CommandResult.Failure(Reason.WrongData)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    suspend fun giveWarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, reason ->
            addWarn(user, command.from!!, reason)
        }
    }

    suspend fun takeWarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            removeWarn(user, command.from!!, newCount)
        }
    }

    suspend fun giveImmunity(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addImmunity(user, command.from!!, newCount)
        }
    }

    suspend fun giveUnwarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addUnwarn(user, command.from!!, newCount)
        }
    }

    suspend fun giveBalance(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, amount ->
            val newAmount = if (amount.isNotBlank()) amount.trim().toInt() else 1
            addBalance(user, command.from!!, newAmount)
        }
    }

    private suspend fun onGiveCommand(
        command: TextMessage,
        content: String,
        action: suspend (ChatUser, String) -> CommandResult
    ): CommandResult {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdmin(command.from?.id?.chatId.toString())) {
            val firstPart = content.split(" ")[0]
            val secondPart = content.removePrefix(firstPart).trim()
            if (firstPart.all { it in '0'..'9' } && firstPart.length >= 8) {
                val chatMember = bot.getChatUserById(command.chat.id, firstPart.toLong())
                val userEntry = repository.getUserById(firstPart) ?: UserEntity(
                    chatMember.id.chatId.toString(),
                    chatMember.firstName,
                    chatMember.username?.username ?: ""
                )
                return action(ChatUser(command.chat.id, userEntry, chatMember), secondPart)
            } else if (firstPart.startsWith("@") && firstPart.length > 1) {
                val userEntry = repository.getUserByUsername(firstPart) ?: return CommandResult.Failure(Reason.UserNotFound)
                val chatMember = bot.getChatUserById(command.chat.id, userEntry.id.toLong())
                return action(ChatUser(command.chat.id, userEntry, chatMember), secondPart)
            } else if (repliedUser != null) {
                val userEntry = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    repliedUser.username?.username ?: ""
                )
                return action(ChatUser(command.chat.id, userEntry, repliedUser), content)
            }

            return CommandResult.Failure(Reason.WrongData)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    private suspend fun addWarn(chatUser: ChatUser, from: User, reason: String): CommandResult {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            val newCount = chatUser.storedUser.adminWarns + 1
            updateWarns(chatUser, newCount)

            val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)
            val message = """
                |Администратору $markdownNameString выдано $newCount\/6 предупреждений\.
                |${if (reason.isNotBlank()) "*Причина:* $reason" else ""}
                """.trimMargin()
            AppContext.journal.write(JournalEvent.WarnsUpdate(
                chatUser.storedUser.id,
                chatUser.storedUser.name,
                from.id.chatId.toString(),
                from.firstName,
                newCount,
                reason
            ))

            return CommandResult.Success(message, chatUser.storedUser.id)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    private suspend fun removeWarn(chatUser: ChatUser, from: User, count: Int = 1): CommandResult {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            if (count <= 0 || count > chatUser.storedUser.adminWarns) {
                return CommandResult.Failure(Reason.WrongCount)
            }

            val newCount = chatUser.storedUser.adminWarns - count
            val countText = if (newCount == 0) "больше не имеет предупреждений\\." else "имеет $newCount\\/6 предупреждений\\."
            val message = "Администратор ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} $countText"
            updateWarns(chatUser, newCount)
            AppContext.journal.write(JournalEvent.WarnsUpdate(
                chatUser.storedUser.id,
                chatUser.storedUser.name,
                from.id.chatId.toString(),
                from.firstName,
                newCount
            ))

            return CommandResult.Success(message, chatUser.storedUser.id)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    private suspend fun addImmunity(chatUser: ChatUser, from: User, count: Int = 1): CommandResult {
        val newCount = chatUser.storedUser.immunities + count
        updateImmunities(chatUser, newCount)
        val actionString = PluralsUtil.pluralize(count, "иммунитет", "подарено")
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString ${actionString}\\."
        AppContext.journal.write(JournalEvent.ItemGiving(
                chatUser.storedUser.id,
                chatUser.storedUser.name,
                from.id.chatId.toString(),
                from.firstName,
                "иммунитет",
                count
        ))
        return CommandResult.Success(message, chatUser.storedUser.id)
    }

    private suspend fun addUnwarn(chatUser: ChatUser, from: User, count: Int = 1): CommandResult {
        val newCount = chatUser.storedUser.unwarns + count
        updateUnwarns(chatUser, newCount)
        val actionString = PluralsUtil.pluralize(count, "анварн", "подарено")
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString ${actionString}\\."
        AppContext.journal.write(JournalEvent.ItemGiving(
            chatUser.storedUser.id,
            chatUser.storedUser.name,
            from.id.chatId.toString(),
            from.firstName,
            "анварн",
            count
        ))
        return CommandResult.Success(message, chatUser.storedUser.id)
    }

    private suspend fun addBalance(chatUser: ChatUser, from: User, amount: Int = 0): CommandResult {
        val newAmount = chatUser.storedUser.balance + amount
        updateBalance(chatUser, newAmount)
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString выдано $amount \uD83D\uDCB8"
        AppContext.journal.write(JournalEvent.ItemGiving(
            chatUser.storedUser.id,
            chatUser.storedUser.name,
            from.id.chatId.toString(),
            from.firstName,
            "валюта",
            amount
        ))
        return CommandResult.Success(message, chatUser.storedUser.id)
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