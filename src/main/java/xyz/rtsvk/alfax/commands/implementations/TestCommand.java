package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class TestCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) {
		chat.sendMessage("**Serus " + user.getMention() + "**");
	}

	@Override
	public String getName() {
		return "test";
	}

	@Override
	public String getDescription() {
		return "Skusobny prikaz na overenie funkcnosti driveru.";
	}

	@Override
	public String getUsage() {
		return "test";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
