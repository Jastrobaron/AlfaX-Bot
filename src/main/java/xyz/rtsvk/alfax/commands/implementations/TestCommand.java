package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;

public class TestCommand implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		channel.createMessage("**Serus " + user.getMention() + "**").block();
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
}
