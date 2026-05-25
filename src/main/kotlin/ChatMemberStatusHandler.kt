package com.ehedgehog

import com.ehedgehog.data.JournalEvent
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onMyChatMemberUpdated
import dev.inmo.tgbotapi.types.chat.member.ChatMember

fun BehaviourContext.registerChatMemberStatusHandler() {
    onMyChatMemberUpdated { member ->
        when (member.newChatMemberState.status) {
            ChatMember.Status.Member -> AppContext.journal.write(
                JournalEvent.NewUser(member.user.id.chatId.toString(), member.user.firstName)
            )
            ChatMember.Status.Kicked -> AppContext.journal.write(
                JournalEvent.UserBlocked(member.user.id.chatId.toString(), member.user.firstName)
            )
            else -> {}
        }
    }
}