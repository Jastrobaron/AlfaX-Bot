package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.ArrayList;
import java.util.List;

public class ChessCommand implements ICommand {

	private List<Pair> players;

	public ChessCommand() {
		this.players = new ArrayList<>();
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String command = args.get(0).toLowerCase();
		String arg = args.get(1).toLowerCase();
		switch (command) {
			case "request":

				break;
			case "abort":
				break;
			case "accept":
				break;
			case "deny":
				break;
			case "move":
				break;
			default:
				chat.sendMessage("Neplatny prikaz " + command);
				break;
		}
	}

	@Override
	public String getName() {
		return "chess";
	}

	@Override
	public String getDescription() {
		return "Command to control your chess game. Each user can participate in one game only.";
	}

	@Override
	public String getUsage() {
		return "chess <request|accept|deny|abort|move> [args]";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}

	private static class Pair {
		Snowflake p1,p2;
	}
}
