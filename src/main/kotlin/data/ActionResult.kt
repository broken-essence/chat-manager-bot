package com.ehedgehog.data

sealed class ActionResult {
    data class Success(val data: String? = null): ActionResult()
    data class Failure(val reason: Reason): ActionResult()
}

sealed class Reason(val code: String) {
    object NotAvailable : Reason("NOT_AVAILABLE")
    object NotEnoughItems : Reason("NOT_ENOUGH_ITEMS")
    object NotEnoughBalance : Reason("NOT_ENOUGH_BALANCE")
    object UserNotFound : Reason("USER_NOT_FOUND")
    object AccessDenied : Reason("ACCESS_DENIED")
    object LimitExceeded: Reason("LIMIT_EXCEEDED")
    object UnexpectedError: Reason("UNEXPECTED_ERROR")
}
