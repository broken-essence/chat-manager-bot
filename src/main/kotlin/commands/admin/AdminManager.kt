package com.ehedgehog.commands.admin

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.UserStatus
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.getChatUserById
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class AdminManager(private val bot: TelegramBot): BaseUserManager(bot) {

    private val repository = UserRepository()

    @OptIn(RiskFeature::class)
    fun changeUserStatus(command: TextMessage, statusValue: Int): CommandResult {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
            if (repliedUser != null && statusValue in 0..<UserStatus.entries.size) {
                val userId = repliedUser.id.chatId.toString()
                val user = repository.getUserById(userId)
                val markdownNameString = createMarkdownLink(repliedUser.firstName, userId)
                val status = UserStatus.fromInt(statusValue)

                setUserStatus(
                    user ?: UserEntity(userId, repliedUser.firstName, repliedUser.username?.username ?: ""),
                    status
                )

                val message = "$markdownNameString теперь *${getStatusDescription(status)}*"
                return CommandResult.Success(message, userId)
            }

            return CommandResult.Failure(Reason.WrongData)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    @OptIn(RiskFeature::class)
    suspend fun giveWarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, reason ->
            addWarn(user, reason)
        }
    }

    suspend fun takeWarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            removeWarn(user, newCount)
        }
    }

    suspend fun giveImmunity(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addImmunity(user, newCount)
        }
    }

    suspend fun giveUnwarn(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, count ->
            val newCount = if (count.isNotBlank()) count.trim().toInt() else 1
            addUnwarn(user, newCount)
        }
    }

    suspend fun giveBalance(command: TextMessage, content: String): CommandResult {
        return onGiveCommand(command, content) { user, amount ->
            val newAmount = if (amount.isNotBlank()) amount.trim().toInt() else 1
            addBalance(user, newAmount)
        }
    }

    @OptIn(RiskFeature::class)
    private suspend fun onGiveCommand(
        command: TextMessage,
        content: String,
        action: suspend (ChatUser, String) -> CommandResult
    ): CommandResult {
        val repliedUser = command.replyTo?.from

        if (isSeniorAdminOrOwner(command.from?.id?.chatId.toString())) {
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

    private fun addWarn(chatUser: ChatUser, reason: String): CommandResult {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            val newCount = chatUser.storedUser.adminWarns + 1
            updateWarns(chatUser, newCount)

            val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)
            val message = """
                |Администратору $markdownNameString выдано $newCount\/6 предупреждений\.
                |${if (reason.isNotBlank()) "*Причина:* $reason" else ""}
                """.trimMargin()
            return CommandResult.Success(message, chatUser.storedUser.id)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    private fun removeWarn(chatUser: ChatUser, count: Int = 1): CommandResult {
        if (chatUser.storedUser.status >= UserStatus.ADMIN) {
            if (count <= 0 || count > chatUser.storedUser.adminWarns) {
                return CommandResult.Failure(Reason.WrongCount)
            }

            val newCount = chatUser.storedUser.adminWarns - count
            val countText = if (newCount == 0) "больше не имеет предупреждений\\." else "имеет $newCount\\/6 предупреждений\\."
            val message = "Администратор ${createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)} $countText"
            updateWarns(chatUser, newCount)

            return CommandResult.Success(message, chatUser.storedUser.id)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    private fun addImmunity(chatUser: ChatUser, count: Int = 1): CommandResult {
        val newCount = chatUser.storedUser.immunities + count
        updateImmunities(chatUser, newCount)
        val actionString = createAmountString("подарен", "иммунитет", count)
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString ${actionString}\\."
        return CommandResult.Success(message, chatUser.storedUser.id)
    }

    private fun addUnwarn(chatUser: ChatUser, count: Int = 1): CommandResult {
        val newCount = chatUser.storedUser.unwarns + count
        updateUnwarns(chatUser, newCount)
        val actionString = createAmountString("подарен", "анварн", count)
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString ${actionString}\\."
        return CommandResult.Success(message, chatUser.storedUser.id)
    }

    private fun addBalance(chatUser: ChatUser, amount: Int = 0): CommandResult {
        val newAmount = chatUser.storedUser.balance + amount
        updateBalance(chatUser, newAmount)
        val markdownNameString = createMarkdownLink(chatUser.chatMember.firstName, chatUser.storedUser.id)

        val message = "Пользователю $markdownNameString выдано $amount \uD83D\uDCB8"
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