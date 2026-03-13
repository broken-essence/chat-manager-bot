package com.ehedgehog

import com.ehedgehog.commands.admin.AdminManager
import com.ehedgehog.commands.admin.registerAdminCommands
import com.ehedgehog.commands.event.EventManager
import com.ehedgehog.commands.event.registerEventCommands
import com.ehedgehog.commands.general.GeneralManager
import com.ehedgehog.commands.general.registerGeneralCommands
import com.ehedgehog.screens.ScreenRouter
import com.ehedgehog.screens.profile.ProfileScreen
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext

fun registerCommands(bot: TelegramBot, context: BehaviourContext) {
    context.registerEventCommands(EventManager(bot))
    context.registerGeneralCommands(GeneralManager(bot))
    context.registerAdminCommands(AdminManager(bot))
}

fun registerScreens(bot: TelegramBot) {
    ScreenRouter.registerScreen(ProfileScreen(bot))
}