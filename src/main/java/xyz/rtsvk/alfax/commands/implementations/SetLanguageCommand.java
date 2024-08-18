package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class SetLanguageCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (args.size() != 1) {
			chat.sendMessage("Invalid number of arguments. Usage: " + this.getUsage());
			return;
		}

		boolean success = Database.setUserLanguage(user.getId(), args.get(0));
		if (success) {
			chat.sendMessage("Language set to " + args.get(0));
		} else {
			chat.sendMessage("Failed to set language");
		}
	}

	@Override
	public String getName() {
		return "setlanguage";
	}

	@Override
	public String getDescription() {
		return "Set the language of the bot for yourself";
	}

	@Override
	public String getUsage() {
		return "setlanguage <language>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("setlang", "lang");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
