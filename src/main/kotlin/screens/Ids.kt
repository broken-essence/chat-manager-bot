package com.ehedgehog.screens

object ScreenIds {
    const val START = "start"
    const val PROFILE = "profile"
    const val INVENTORY = "inventory"
    const val SHOP = "shop"
    const val IMMUNITY_QUEUE = "immunity_queue"
    const val REQUEST_UNWARN = "request_unwarn"
    const val HELP = "help"
    const val PROPOSAL = "propose"
    const val DIVORCE = "divorce"
}

object ActionIds {
    const val USE_UNWARN = "action:${ScreenIds.INVENTORY}/use_unwarn"
    const val USE_IMMUNITY = "action:${ScreenIds.INVENTORY}/use_immunity"
    const val BUY_UNWARN = "action:${ScreenIds.SHOP}/buy_unwarn"
    const val BUY_IMMUNITY = "action:${ScreenIds.SHOP}/buy_immunity"
    const val BUY_RING = "action:${ScreenIds.SHOP}/buy_ring"
    const val IMMUNITY_QUEUE_CONFIRM = "action:${ScreenIds.IMMUNITY_QUEUE}/confirm"
    const val IMMUNITY_QUEUE_DECLINE = "action:${ScreenIds.IMMUNITY_QUEUE}/decline"
    const val UNWARN_CONFIRM = "action:${ScreenIds.REQUEST_UNWARN}/confirm_unwarn"
    const val UNWARN_DECLINE = "action:${ScreenIds.REQUEST_UNWARN}/decline_unwarn"
    const val PROPOSAL_ACCEPT = "action:${ScreenIds.PROPOSAL}/accept_proposal"
    const val PROPOSAL_REJECT = "action:${ScreenIds.PROPOSAL}/reject_proposal"
    const val DIVORCE_CONFIRM = "action:${ScreenIds.DIVORCE}/confirm_divorce"
    const val DIVORCE_DECLINE = "action:${ScreenIds.DIVORCE}/decline_divorce"
}