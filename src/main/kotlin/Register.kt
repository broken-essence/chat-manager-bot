package com.ehedgehog

import com.ehedgehog.commands.admin.AdminManager
import com.ehedgehog.commands.admin.registerAdminCommands
import com.ehedgehog.commands.event.EventManager
import com.ehedgehog.commands.event.registerEventCommands
import com.ehedgehog.commands.general.GeneralManager
import com.ehedgehog.commands.general.registerGeneralCommands
import com.ehedgehog.screens.ActionRouter
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.screens.immunity_queue.ImmunityQueueConfirmAction
import com.ehedgehog.screens.immunity_queue.ImmunityQueueDeclineAction
import com.ehedgehog.screens.immunity_queue.ImmunityQueueManager
import com.ehedgehog.screens.immunity_queue.ImmunityQueueScreen
import com.ehedgehog.screens.inventory.InventoryManager
import com.ehedgehog.screens.inventory.InventoryScreen
import com.ehedgehog.screens.inventory.UseImmunityAction
import com.ehedgehog.screens.inventory.UseUnwarnAction
import com.ehedgehog.screens.profile.ProfileManager
import com.ehedgehog.screens.profile.ProfileScreen
import com.ehedgehog.screens.request_unwarn.ConfirmUnwarnAction
import com.ehedgehog.screens.request_unwarn.DeclineUnwarnAction
import com.ehedgehog.screens.request_unwarn.UnwarnRequestManager
import com.ehedgehog.screens.request_unwarn.UnwarnRequestScreen
import com.ehedgehog.screens.shop.BuyImmunityAction
import com.ehedgehog.screens.shop.BuyUnwarnAction
import com.ehedgehog.screens.shop.ShopManager
import com.ehedgehog.screens.shop.ShopScreen
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext

fun registerCommands(bot: TelegramBot, context: BehaviourContext) {
    context.registerEventCommands(EventManager(bot))
    context.registerGeneralCommands(GeneralManager(bot))
    context.registerAdminCommands(AdminManager(bot))
}

fun registerScreens(bot: TelegramBot) {
    ScreenRouter.registerScreen(ProfileScreen(ProfileManager(bot)))
    ScreenRouter.registerScreen(InventoryScreen(InventoryManager(bot)))
    ScreenRouter.registerScreen(ShopScreen(ShopManager(bot)))
    ScreenRouter.registerScreen(UnwarnRequestScreen(UnwarnRequestManager(bot)))
    ScreenRouter.registerScreen(ImmunityQueueScreen(ImmunityQueueManager(bot)))
}

fun registerActions(bot: TelegramBot) {
    ActionRouter.registerAction(UseUnwarnAction(bot, InventoryManager(bot)))
    ActionRouter.registerAction(UseImmunityAction(bot,InventoryManager(bot)))
    ActionRouter.registerAction(BuyUnwarnAction(bot, ShopManager(bot)))
    ActionRouter.registerAction(BuyImmunityAction(bot, ShopManager(bot)))
    ActionRouter.registerAction(ConfirmUnwarnAction(bot, UnwarnRequestManager(bot)))
    ActionRouter.registerAction(DeclineUnwarnAction(bot, UnwarnRequestManager(bot)))
    ActionRouter.registerAction(ImmunityQueueConfirmAction(bot, ImmunityQueueManager(bot)))
    ActionRouter.registerAction(ImmunityQueueDeclineAction(bot))
}