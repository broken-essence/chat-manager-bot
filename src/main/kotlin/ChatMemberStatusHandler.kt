package com.ehedgehog

import com.ehedgehog.data.JournalEvent
import com.ehedgehog.database.repositories.UserRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMyChatMemberUpdated
import dev.inmo.tgbotapi.types.chat.member.ChatMember

fun BehaviourContext.registerChatMemberStatusHandler() {
    onMyChatMemberUpdated { member ->
        when (member.newChatMemberState.status) {
            ChatMember.Status.Kicked -> {
                UserRepository().setActivated(member.user.id.chatId.toString(), false)
                AppContext.journal.write(
                    JournalEvent.UserLeft(member.user.id.chatId.toString(), member.user.firstName)
                )
            }
            else -> {}
        }
    }
}