package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;
import java.util.Properties;
import java.util.Random;

public class PickCommand implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		if (!args.isEmpty()) {
			Random random = new Random();
			int index = random.nextInt(args.size()-1);
			channel.createMessage("**" + user.getMention() + ", " + args.get(index+1) + "**").block();
		}
		else channel.createMessage("**Potrebujem minimalne 2 moznosti, aby som mohol rozhodnnut!**").block();
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
}
