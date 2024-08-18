package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;
import java.util.Random;

public class PickCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) {
		if (args.isEmpty()) {
			chat.sendMessage("**Potrebujem minimalne 2 moznosti, aby som mohol rozhodnnut!**");
			return;
		}
		Random random = new Random();
		int index = random.nextInt(args.size()-1);
		chat.sendMessage("**" + user.getMention() + ", " + args.get(index+1) + "**");
	}

	@Override
	public String getName() {
		return "pick";
	}

	@Override
	public String getDescription() {
		return "Nahodne vyberie jednu z ponukanych moznosti.";
	}

	@Override
	public String getUsage() {
		return "pick <moznost1> <moznost2> [moznost3] [moznost4] [...]";
	}

	@Override
	public List<String> getAliases() {
		return List.of("choose");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
