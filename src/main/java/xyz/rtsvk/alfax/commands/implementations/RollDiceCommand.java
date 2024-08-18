package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;
import java.util.Random;

public class RollDiceCommand implements ICommand {

	private Random rand;

	public RollDiceCommand() {
		this.rand = new Random();
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		final int N = (args.isEmpty() ? 6 : Integer.parseInt(args.get(0))) + 1;
		chat.sendMessage("**" + rand.nextInt(N) + "**");
	}

	@Override
	public String getName() {
		return "roll";
	}

	@Override
	public String getDescription() {
		return "Rolls the dice!";
	}

	@Override
	public String getUsage() {
		return "roll [maximum]";
	}

	@Override
	public List<String> getAliases() {
		return List.of("dice", "hod");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
