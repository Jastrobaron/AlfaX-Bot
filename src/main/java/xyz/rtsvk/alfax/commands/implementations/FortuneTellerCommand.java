package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;
import java.util.Random;

public class FortuneTellerCommand implements Command {
	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) {
		if (args.size() == 1) {
			chat.sendMessage(language.getMessage("command.8ball.no-question"));
		}

		List<String> answers = language.matchMessages("command.8ball.answer.*");
		int index = Math.toIntExact(Math.round(Math.random() * answers.size()));
		chat.sendMessage("**" + answers.get(index) + "**");
	}

	@Override
	public String getName() {
		return "8ball";
	}

	@Override
	public String getDescription() {
		return "command.8ball.description";
	}

	@Override
	public String getUsage() {
		return "8ball <otazka>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("8b", "fortune");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
