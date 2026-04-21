package com.ehedgehog.data

import kotlin.String

sealed class ActionResult {
    data class Success(val data: String? = null): ActionResult()
    data class Failure(val reason: Reason): ActionResult()
}

sealed class CommandResult {
    data class Success(val message: String? = null, val targetUserId: String? = null): CommandResult()
    data class Failure(val reason: Reason): CommandResult()
}

sealed class Reason(val code: String) {
    object NotAvailable : Reason("NOT_AVAILABLE")
    object NotEnoughItems : Reason("NOT_ENOUGH_ITEMS")
    object NotEnoughBalance : Reason("NOT_ENOUGH_BALANCE")
    object UserNotFound : Reason("USER_NOT_FOUND")
    object AccessDenied : Reason("ACCESS_DENIED")
    object LimitExceeded: Reason("LIMIT_EXCEEDED")
    object WrongData: Reason("WRONG_DATA")
    object WrongCount: Reason("WRONG_COUNT")
    object UnexpectedError: Reason("UNEXPECTED_ERROR")
}
