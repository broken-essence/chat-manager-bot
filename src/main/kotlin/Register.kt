package com.ehedgehog

import com.ehedgehog.commands.admin.AdminManager
import com.ehedgehog.commands.admin.registerAdminCommands
import com.ehedgehog.commands.event.EventManager
import com.ehedgehog.commands.event.registerEventCommands
import com.ehedgehog.commands.general.GeneralManager
import com.ehedgehog.commands.general.registerGeneralCommands
import com.ehedgehog.screens.ActionRouter
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.screens.inventory.InventoryManager
import com.ehedgehog.screens.inventory.InventoryScreen
import com.ehedgehog.screens.inventory.UseImmunityAction
import com.ehedgehog.screens.inventory.UseUnwarnAction
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
    ScreenRouter.registerScreen(ProfileScreen(bot))
    ScreenRouter.registerScreen(InventoryScreen(bot))
    ScreenRouter.registerScreen(ShopScreen(bot))
    ScreenRouter.registerScreen(UnwarnRequestScreen(bot))
}

fun registerActions(bot: TelegramBot) {
    ActionRouter.registerAction(UseUnwarnAction(InventoryManager(bot)))
    ActionRouter.registerAction(UseImmunityAction(InventoryManager(bot)))
    ActionRouter.registerAction(BuyUnwarnAction(ShopManager(bot)))
    ActionRouter.registerAction(BuyImmunityAction(ShopManager(bot)))
    ActionRouter.registerAction(ConfirmUnwarnAction(UnwarnRequestManager(bot)))
    ActionRouter.registerAction(DeclineUnwarnAction(UnwarnRequestManager(bot)))
}