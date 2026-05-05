package com.ehedgehog.commands.event

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class EventManager : BaseUserManager() {

    private val repository = UserRepository()

    @OptIn(RiskFeature::class)
    fun giveEventPoints(command: TextMessage, args: Array<String>): CommandResult {
        val count = if (args.isNotEmpty()) args[0].toInt() else 1
        val repliedUser = command.replyTo?.from
        val markdownNameString = repliedUser?.let { createMarkdownLink(it.firstName, it.id.chatId.toString()) }

        if (repliedUser != null && count > 0) {
            if (isAdmin(command.from?.id?.chatId.toString())) {
                val storedUser = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    repliedUser.username?.username ?: ""
                )
                val newCount = storedUser.eventPoints + count
                updateEventPoints(ChatUser(command.chat.id, storedUser, repliedUser), newCount)
                val amountString = createAmountString("начислен", "что\\-то", count)

                val message = "Пользователю $markdownNameString $amountString\\!\nВсего печенюшек: $newCount 🍪"
                return CommandResult.Success(message, storedUser.id)
            }

            return CommandResult.Failure(Reason.AccessDenied)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    @OptIn(RiskFeature::class)
    fun takeEventPoints(command: TextMessage, args: Array<String>): CommandResult {
        val count = if (args.isNotEmpty()) args[0].toInt() else 1
        val repliedUser = command.replyTo?.from ?: return CommandResult.Failure(Reason.WrongData)
        val markdownNameString = createMarkdownLink(repliedUser.firstName, repliedUser.id.chatId.toString())
        val storedUser = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
            repliedUser.id.chatId.toString(),
            repliedUser.firstName,
            repliedUser.username?.username ?: ""
        )

        if (isAdmin(command.from?.id?.chatId.toString())) {
            if (storedUser.eventPoints > 0 && count <= storedUser.eventPoints) {
                val newCount = storedUser.eventPoints - count
                updateEventPoints(ChatUser(command.chat.id, storedUser, repliedUser), newCount)
                val amountString = createAmountString("отобран", "что\\-то", count)

                val message = "У пользователя $markdownNameString $amountString\\!\nВсего печенюшек: $newCount 🍪"
                return CommandResult.Success(message, storedUser.id)
            }

            return CommandResult.Failure(Reason.NotEnoughBalance)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun getEventPointRating(): CommandResult {
        val eventPointList = repository.getTopByEventPoints()
        val ratingString = formatRatingList(eventPointList)

        val message = "\uD83C\uDF6A *Рейтинг печенюшек:*\n\n".plus(ratingString)
        return CommandResult.Success(message)
    }

    @OptIn(RiskFeature::class)
    fun getPersonalRating(command: TextMessage): CommandResult {
        val user = command.from ?: return CommandResult.Failure(Reason.UserNotFound)

        val userMarkdown = createMarkdownLink(user.firstName, user.id.chatId.toString())
        val eventPointCount = repository.getEventPointCountById(user.id.chatId.toString())

        val message = "\uD83C\uDF85 Пользователь ${userMarkdown}\n\uD83C\uDF81 *Ваш баланс:* $eventPointCount \uD83C\uDF6A"
        return CommandResult.Success(message)
    }

    @OptIn(RiskFeature::class)
    fun clearEventPoints(command: TextMessage): CommandResult {
        if (isSeniorAdmin(command.from?.id?.chatId.toString())) {
            repository.clearEventPoints()
            return CommandResult.Success("Очки пользователей обнулены.")
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun showCommands(): CommandResult {
        val message = """*ᅠ   Команды бота:*
                |👮🏼 /cookie – подарить печенюшку \(reply\)
                |👮🏼 /take – забрать печенюшку \(reply\)
                |🪿 /rating – рейтинг печенюшек
                |🪿 /balance – посмотреть баланс печенюшек
                |🪿 /hint – список команд
            """.trimMargin()
        return CommandResult.Success(message)
    }

    private fun formatRatingList(list: List<UserEntity>): String =
        if (list.isNotEmpty()) {
            list.mapIndexed { index, user ->
                "${index + 1}\\. ${createMarkdownLink(user.name, user.id)} — ${user.eventPoints} \uD83C\uDF6A"
            }.joinToString("\n")
        } else "Список пуст\\."

}