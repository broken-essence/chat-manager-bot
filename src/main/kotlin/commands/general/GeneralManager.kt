package com.ehedgehog.commands.general

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.Reason
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.bot.TelegramBot

class GeneralManager(bot: TelegramBot): BaseUserManager(bot) {

    val repository = UserRepository()

    fun showImmunitiesList(): CommandResult {
        val immunities = repository.getUsersWithActiveImmunity()
        val immunitiesListString = formatImmunitiesList(immunities)

        val message = """
            *Игроки с активным иммунитетом:
            |
            |*$immunitiesListString
            |
            |❗ Иммунитет обозначается эмодзи 🚩 в нике\. Игроков с иммунитетом *запрещено в первые 2 игровые ночи* убивать и посещать активным ролям\.
            """.trimMargin()

        return CommandResult.Success(message)
    }

    fun randomize(args: Array<String>): CommandResult {
        val count = if (args.size > 1) args[1].toInt() else 1
        val until = args.firstOrNull()?.toInt() ?: 10

        if (count <= 0 || until <= 0 || until < count) {
            return CommandResult.Failure(Reason.WrongData)
        }

        val resultString = (1..until).shuffled().take(count).joinToString()

        return CommandResult.Success("\uD83C\uDFB2 *Рандом от 1 до ${until}:*\n\n${resultString}")
    }

    private fun formatImmunitiesList(list: List<UserEntity>): String =
        if (list.isNotEmpty()) {
            list.mapIndexed { index, user ->
                "${index + 1}\\. ${createMarkdownLink(user.name, user.id)} — ${getImmunityStatus(user)}"
            }.joinToString("\n")
        } else "Список пуст\\."

}