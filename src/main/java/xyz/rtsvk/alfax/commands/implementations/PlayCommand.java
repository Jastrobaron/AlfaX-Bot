package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class PlayCommand implements ICommand {

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

	}

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public String getDescription() {
		return "Joins the voice channel you are in and plays the specified song.";
	}

	@Override
	public String getUsage() {
		return "play <link>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("p");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
