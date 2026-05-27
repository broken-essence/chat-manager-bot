package com.ehedgehog.commands.event

import com.ehedgehog.AppContext
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.EventConfig
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import com.ehedgehog.utils.PluralsUtil
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

@OptIn(RiskFeature::class)
class EventManager : BaseUserManager() {

    private val repository = UserRepository()

    fun startEvent(command: TextMessage, args: Array<String>): CommandResult {
        if (isSeniorAdmin(command.from?.id?.chatId.toString())) {
            val config = if (args.size >= 2)
                EventConfig(true, args[0], args[1])
            else AppContext.settings.getEventConfig().copy(enabled = true)

            AppContext.settings.setEventConfig(config)
            repository.clearEventPoints()
            val message = """*Событие запущено\!*
                |
                |Генерируем:
                |– ${PluralsUtil.pluralize(1, config.noun, "начислено")}
                |– ${PluralsUtil.pluralize(3, config.noun, "начислено")}
                |– ${PluralsUtil.pluralize(7, config.noun, "начислено")}
                |
                |_Если генерация не удалась, соболезную\.\.\._
            """.trimMargin()
            return CommandResult.Success(message)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun stopEvent(command: TextMessage): CommandResult {
        if (isSeniorAdmin(command.from?.id?.chatId.toString())) {
            val config = AppContext.settings.getEventConfig().copy(enabled = false)
            AppContext.settings.setEventConfig(config)
            return CommandResult.Success("Событие остановлено!")
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun giveEventPoints(command: TextMessage, args: Array<String>): CommandResult {
        val count = if (args.isNotEmpty()) args[0].toInt() else 1
        val repliedUser = command.replyTo?.from
        val markdownNameString = repliedUser?.let { createMarkdownLink(it.firstName, it.id.chatId.toString()) }

        val eventConfig = AppContext.settings.getEventConfig()
        if (!eventConfig.enabled) return CommandResult.Failure(Reason.EventNotEnabled)

        if (repliedUser != null && count > 0) {
            if (isAdmin(command.from?.id?.chatId.toString())) {
                val storedUser = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
                    repliedUser.id.chatId.toString(),
                    repliedUser.firstName,
                    repliedUser.username?.username ?: ""
                )
                val newCount = storedUser.eventPoints + count
                updateEventPoints(ChatUser(command.chat.id, storedUser, repliedUser), newCount)

                val countString = PluralsUtil.pluralize(count, eventConfig.noun, "начислено")
                val pointsName = PluralsUtil.getPlurals(eventConfig.noun).many
                val message = "Пользователю $markdownNameString $countString\\!\nВсего $pointsName: $newCount ${eventConfig.emoji}"
                return CommandResult.Success(message, storedUser.id)
            }

            return CommandResult.Failure(Reason.AccessDenied)
        }

        return CommandResult.Failure(Reason.WrongData)
    }

    fun takeEventPoints(command: TextMessage, args: Array<String>): CommandResult {
        val count = if (args.isNotEmpty()) args[0].toInt() else 1
        val repliedUser = command.replyTo?.from ?: return CommandResult.Failure(Reason.WrongData)
        val markdownNameString = createMarkdownLink(repliedUser.firstName, repliedUser.id.chatId.toString())
        val storedUser = repository.getUserById(repliedUser.id.chatId.toString()) ?: UserEntity(
            repliedUser.id.chatId.toString(),
            repliedUser.firstName,
            repliedUser.username?.username ?: ""
        )

        val eventConfig = AppContext.settings.getEventConfig()
        if (!eventConfig.enabled) return CommandResult.Failure(Reason.EventNotEnabled)

        if (isAdmin(command.from?.id?.chatId.toString())) {
            if (storedUser.eventPoints > 0 && count <= storedUser.eventPoints) {
                val newCount = storedUser.eventPoints - count
                updateEventPoints(ChatUser(command.chat.id, storedUser, repliedUser), newCount)

                val countString = PluralsUtil.pluralize(count, eventConfig.noun, "отобрано")
                val pointsName = PluralsUtil.getPlurals(eventConfig.noun).many
                val message = "У пользователя $markdownNameString $countString\\!\nВсего $pointsName: $newCount ${eventConfig.emoji}"
                return CommandResult.Success(message, storedUser.id)
            }

            return CommandResult.Failure(Reason.NotEnoughBalance)
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun getEventPointRating(): CommandResult {
        val eventPointList = repository.getTopByEventPoints()
        val eventConfig = AppContext.settings.getEventConfig()
        val ratingString = formatRatingList(eventPointList, eventConfig.emoji)

        val pointsName = PluralsUtil.getPlurals(eventConfig.noun).many
        val isEventFinishedString = if (!eventConfig.enabled) "\n\nСобытие завершено\\!" else ""
        val message = "${eventConfig.emoji} *Рейтинг $pointsName:*\n\n$ratingString$isEventFinishedString"
        return CommandResult.Success(message)
    }

    @OptIn(RiskFeature::class)
    fun getPersonalRating(command: TextMessage): CommandResult {
        val user = command.from ?: return CommandResult.Failure(Reason.UserNotFound)

        val userMarkdown = createMarkdownLink(user.firstName, user.id.chatId.toString())
        val eventPointCount = repository.getEventPointCountById(user.id.chatId.toString())
        val eventConfig = AppContext.settings.getEventConfig()
        val pointsName = PluralsUtil.getPlurals(eventConfig.noun).many

        val message = "\uD83E\uDEBF Пользователь ${userMarkdown}\n" +
                "\uD83C\uDF81 *Получено $pointsName:* $eventPointCount ${eventConfig.emoji}"
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

    private fun formatRatingList(list: List<UserEntity>, emoji: String): String =
        if (list.isNotEmpty()) {
            list.mapIndexed { index, user ->
                "${index + 1}\\. ${createMarkdownLink(user.name, user.id)} — ${user.eventPoints} $emoji"
            }.joinToString("\n")
        } else "Список пуст\\."

}