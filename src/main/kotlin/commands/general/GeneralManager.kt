package com.ehedgehog.commands.general

import com.ehedgehog.AppContext
import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.CommandResult
import com.ehedgehog.data.JournalEvent
import com.ehedgehog.data.Reason
import com.ehedgehog.database.ChatUser
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.RiskFeature

class GeneralManager : BaseUserManager() {

    private sealed class Gift{
        object Unwarn: Gift()
        object Immunity: Gift()
        data class Balance(val amount: Int): Gift()
    }

    val repository = UserRepository()

    @OptIn(RiskFeature::class)
    suspend fun showStartScreen(command: TextMessage): CommandResult {
        val from = command.from ?: return CommandResult.Failure(Reason.UnexpectedError)

        if (command.chat.id.chatId.toString() == from.id.chatId.toString()) {
            if (!repository.hasActivatedBot(from.id.chatId.toString())) {
                val user = repository.getUserById(from.id.chatId.toString()) ?: UserEntity(
                    from.id.chatId.toString(), from.firstName, from.username?.username ?: ""
                )
                updateUserEntry(user.copy(
                    name = from.firstName,
                    username = from.username?.username ?: "",
                    isActive = true
                ))
                AppContext.journal.write(
                    JournalEvent.NewUser(from.id.chatId.toString(), from.firstName)
                )
            }
            return CommandResult.Success()
        }

        return CommandResult.Failure(Reason.AccessDenied)
    }

    fun showImmunitiesList(): CommandResult {
        val immunities = repository.getUsersWithActiveImmunity()
        val immunitiesListString = formatImmunitiesList(immunities)

        val message = "*Игроки с активным иммунитетом:\n\n*$immunitiesListString\n\n" +
                "❗ Иммунитет обозначается эмодзи 🥦 в нике\\. Игроков с иммунитетом *запрещено в первые 2 игровые ночи* " +
                "убивать и посещать активным ролям\\. Действует в играх *от 10 человек*\\."

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

    @OptIn(RiskFeature::class)
    fun gift(command: TextMessage, args: Array<String>): CommandResult {
        val from = command.from ?: return CommandResult.Failure(Reason.UserNotFound)
        val target = command.replyTo?.from ?: return CommandResult.Failure(Reason.WrongData)

        if (target.id == from.id) return CommandResult.Failure(Reason.WrongData)

        val fromUserEntry = repository.getUserById(from.id.chatId.toString()) ?: return CommandResult.Failure(Reason.UserNotFound)
        val fromMarkdownNameString = createMarkdownLink(from.firstName, from.id.chatId.toString())
        val targetUserEntry = repository.getUserById(target.id.chatId.toString()) ?: UserEntity(
            target.id.chatId.toString(), target.firstName, target.username?.username ?: ""
        )
        val targetMarkdownNameString = createMarkdownLink(target.firstName, target.id.chatId.toString())

        val giftedItem = applyGift(
            parseGift(args.firstOrNull()) ?: return CommandResult.Failure(Reason.WrongData),
            ChatUser(command.chat.id, fromUserEntry, from),
            ChatUser(command.chat.id, targetUserEntry, target)
        ) ?: return CommandResult.Failure(Reason.NotEnoughItems)

        val message = "\uD83C\uDF81 $fromMarkdownNameString подарил $targetMarkdownNameString $giftedItem\\."
        return CommandResult.Success(message, targetUserEntry.id)
    }

    private fun formatImmunitiesList(list: List<UserEntity>): String =
        if (list.isNotEmpty()) {
            list.mapIndexed { index, user ->
                "${index + 1}\\. ${createMarkdownLink(user.name, user.id)} — ${getImmunityStatus(user)}"
            }.joinToString("\n")
        } else "Список пуст\\."

    private fun applyGift(gift: Gift, fromUser: ChatUser, targetUser: ChatUser): String? {
        return when (gift) {
            is Gift.Unwarn -> if (fromUser.storedUser.unwarns > 0) {
                updateUnwarns(fromUser, fromUser.storedUser.unwarns - 1)
                updateUnwarns(targetUser, targetUser.storedUser.unwarns + 1)
                "снятие предупреждения"
            } else null

            is Gift.Immunity -> if (fromUser.storedUser.immunities > 0) {
                updateImmunities(fromUser, fromUser.storedUser.immunities - 1)
                updateImmunities(targetUser, targetUser.storedUser.immunities + 1)
                "иммунитет"
            } else null

            is Gift.Balance -> if (fromUser.storedUser.balance >= gift.amount) {
                updateBalance(fromUser, fromUser.storedUser.balance - gift.amount)
                updateBalance(targetUser, targetUser.storedUser.balance + gift.amount)
                "${gift.amount} \uD83D\uDCB8 на баланс"
            } else null
        }
    }

    private fun parseGift(giftText: String?): Gift? = when {
        giftText.equals("unwarn") -> Gift.Unwarn
        giftText.equals("immunity") -> Gift.Immunity
        giftText?.all { it in '0'..'9' } == true && giftText.toInt() > 0 -> Gift.Balance(giftText.toInt())
        else -> null
    }

}