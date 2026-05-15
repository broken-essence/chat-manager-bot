package com.ehedgehog.data

sealed class JournalEvent {

    data class Purchase(val userId: String, val name: String, val item: String): JournalEvent()

    data class Activation(val userId: String, val name: String, val item: String): JournalEvent()

    data class NewUser(val userId: String, val name: String): JournalEvent()

    data class WarnsUpdate(
        val userId: String,
        val name: String,
        val fromId: String,
        val fromName: String,
        val newCount: Int,
        val reason: String? = null
    ): JournalEvent()

    data class ItemGiving(
        val userId: String,
        val name: String,
        val fromId: String,
        val fromName: String,
        val item: String,
        val count: Int
    ): JournalEvent()

}