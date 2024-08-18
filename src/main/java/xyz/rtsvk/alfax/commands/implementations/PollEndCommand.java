package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;
import java.util.Map;

public class PollEndCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (chat.getChannel() instanceof PrivateChannel pc) {
			pc.createMessage("You can't create a poll in a private channel").block();
			return;
		}

		boolean pollEnded = Database.endPoll(chat.getChannel().getId());
		if (!pollEnded)
			chat.sendMessage("An error occurred while ending the poll");
		else {
			chat.sendMessage("Poll ended");
			Map<String, Integer> results = Database.getLastPollResults(chat.getChannel().getId());
			StringBuilder pollResults = new StringBuilder();
			results.forEach((option, votes) -> pollResults.append(option).append(": ").append(votes).append("\n"));
			chat.sendMessage("```\n" + pollResults.toString() + "```");
		}

	}

	@Override
	public String getName() {
		return "endpoll";
	}

	@Override
	public String getDescription() {
		return "End the poll you've created.";
	}

	@Override
	public String getUsage() {
		return "endpoll";
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
