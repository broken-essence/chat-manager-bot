package com.ehedgehog

import com.ehedgehog.commands.admin.AdminManager
import com.ehedgehog.commands.admin.registerAdminCommands
import com.ehedgehog.commands.event.EventManager
import com.ehedgehog.commands.event.registerEventCommands
import com.ehedgehog.commands.general.GeneralManager
import com.ehedgehog.commands.general.registerGeneralCommands
import com.ehedgehog.commands.marriages.MarriageManager
import com.ehedgehog.commands.marriages.registerMarriageCommands
import com.ehedgehog.screens.ActionRouter
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.screens.help.HelpManager
import com.ehedgehog.screens.help.HelpScreen
import com.ehedgehog.screens.immunity_queue.ImmunityQueueConfirmAction
import com.ehedgehog.screens.immunity_queue.ImmunityQueueDeclineAction
import com.ehedgehog.screens.immunity_queue.ImmunityQueueManager
import com.ehedgehog.screens.immunity_queue.ImmunityQueueScreen
import com.ehedgehog.screens.inventory.InventoryManager
import com.ehedgehog.screens.inventory.InventoryScreen
import com.ehedgehog.screens.inventory.UseImmunityAction
import com.ehedgehog.screens.inventory.UseUnwarnAction
import com.ehedgehog.screens.marriage.MarriageScreensManager
import com.ehedgehog.screens.marriage.ProposalScreen
import com.ehedgehog.screens.profile.ProfileManager
import com.ehedgehog.screens.profile.ProfileScreen
import com.ehedgehog.screens.request_unwarn.ConfirmUnwarnAction
import com.ehedgehog.screens.request_unwarn.DeclineUnwarnAction
import com.ehedgehog.screens.request_unwarn.UnwarnRequestManager
import com.ehedgehog.screens.request_unwarn.UnwarnRequestScreen
import com.ehedgehog.screens.shop.BuyImmunityAction
import com.ehedgehog.screens.shop.BuyRingAction
import com.ehedgehog.screens.shop.BuyUnwarnAction
import com.ehedgehog.screens.shop.ShopManager
import com.ehedgehog.screens.shop.ShopScreen
import com.ehedgehog.screens.start.StartManager
import com.ehedgehog.screens.start.StartScreen
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext

fun registerCommands(bot: TelegramBot, context: BehaviourContext) {
    context.registerEventCommands(EventManager())
    context.registerGeneralCommands(GeneralManager())
    context.registerAdminCommands(AdminManager(bot))
    context.registerMarriageCommands(MarriageManager())
}

fun registerScreens(bot: TelegramBot) {
    ScreenRouter.registerScreen(StartScreen(StartManager()))
    ScreenRouter.registerScreen(HelpScreen(HelpManager()))
    ScreenRouter.registerScreen(ProfileScreen(ProfileManager()))
    ScreenRouter.registerScreen(InventoryScreen(InventoryManager(bot)))
    ScreenRouter.registerScreen(ShopScreen(ShopManager()))
    ScreenRouter.registerScreen(UnwarnRequestScreen(UnwarnRequestManager()))
    ScreenRouter.registerScreen(ImmunityQueueScreen(ImmunityQueueManager(bot)))
    ScreenRouter.registerScreen(ProposalScreen(MarriageScreensManager(bot)))
}

fun registerActions(bot: TelegramBot) {
    ActionRouter.registerAction(UseUnwarnAction(bot, InventoryManager(bot)))
    ActionRouter.registerAction(UseImmunityAction(bot,InventoryManager(bot)))
    ActionRouter.registerAction(BuyUnwarnAction(bot, ShopManager()))
    ActionRouter.registerAction(BuyImmunityAction(bot, ShopManager()))
    ActionRouter.registerAction(BuyRingAction(bot, ShopManager()))
    ActionRouter.registerAction(ConfirmUnwarnAction(bot, UnwarnRequestManager()))
    ActionRouter.registerAction(DeclineUnwarnAction(bot, UnwarnRequestManager()))
    ActionRouter.registerAction(ImmunityQueueConfirmAction(bot, ImmunityQueueManager(bot)))
    ActionRouter.registerAction(ImmunityQueueDeclineAction(bot))
}