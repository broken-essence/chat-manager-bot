package com.ehedgehog.base

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DATE_FORMAT = "dd MMMM yyyy HH:mm"

abstract class BaseManager {

    fun createMarkdownLink(name: String, userId: String): String = "[${handleReservedSymbols(name)}](tg://user?id=${userId})"

    fun handleReservedSymbols(text: String): String {
        val reservedChars = listOf(
            '_', '*', '[', ']', '(', ')', '~', '`', '>', '#',
            '+', '-', '=', '|', '{', '}', '.', '!'
        )

        var resultString = text
        for (ch in reservedChars) {
            resultString = resultString.replace(ch.toString(), "\\$ch")
        }

        return resultString
    }

    fun dateFromMillis(millis: Long): String =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("UTC+03"))
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT))

}