package com.ehedgehog.screens.request_unwarn

import com.ehedgehog.base.BaseUserManager
import com.ehedgehog.data.ActionResult
import com.ehedgehog.data.Reason
import com.ehedgehog.data.ScreenContext
import com.ehedgehog.database.UserEntity
import com.ehedgehog.database.repositories.UnwarnRequestRepository
import com.ehedgehog.database.repositories.UserRepository

class UnwarnRequestManager : BaseUserManager() {

    val unwarnRequestRepository = UnwarnRequestRepository()
    val userRepository = UserRepository()

    fun getUnwarnRequestMessage(userId: String, name: String): String {
        val markdownLink = createMarkdownLink(name, userId)
        return "⚠\uFE0F Пользователь $markdownLink \\[`${userId}`\\] запрашивает снятие варна\\!"
    }

    fun confirmUnwarn(context: ScreenContext, data: String?): ActionResult {
        if (isSeniorAdmin(context.user.id.chatId.toString())) {
            val user = getUserFromRequest(data) ?: return ActionResult.Failure(Reason.UserNotFound)

            val adminMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
            val newMessage = getUnwarnRequestMessage(user.id, user.name)
                .plus("\n\n✅ *Снятие подтверждено*\n — \uD83D\uDC6E\uD83C\uDFFC $adminMarkdownLink \\[`${context.user.id.chatId}`\\]")
            data?.let { unwarnRequestRepository.deleteRequest(it.toInt()) }
            return ActionResult.Success(newMessage)
        }

        return ActionResult.Failure(Reason.AccessDenied)
    }

    fun declineUnwarn(context: ScreenContext, data: String?): ActionResult {
        if (isSeniorAdmin(context.user.id.chatId.toString())) {
            val user = getUserFromRequest(data) ?: return ActionResult.Failure(Reason.UserNotFound)

            val adminMarkdownLink = createMarkdownLink(context.user.firstName, context.user.id.chatId.toString())
            val newMessage = getUnwarnRequestMessage(user.id, user.name)
                .plus("\n\n❌ *Снятие отклонено*\n — \uD83D\uDC6E\uD83C\uDFFC $adminMarkdownLink \\[`${context.user.id.chatId}`\\]")

            userRepository.updateUnwarnCount(user.id, user.unwarns + 1)
            data?.let { unwarnRequestRepository.deleteRequest(it.toInt()) }
            return ActionResult.Success(newMessage)
        }

        return ActionResult.Failure(Reason.AccessDenied)
    }

    private fun getUserFromRequest(requestId: String?): UserEntity? {
        return requestId?.toInt()?.let {
            val request = unwarnRequestRepository.getRequest(it) ?: return null
            userRepository.getUserById(request.userId)
        }
    }

}