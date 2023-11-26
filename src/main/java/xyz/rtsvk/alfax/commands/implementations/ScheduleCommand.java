package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;

public class ScheduleCommand implements Command {

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		if (!user.getTag().equals("Jastrobaron#0262")) {
			channel.createMessage("**Dobry pokus zmrde. Toto ty nemozes :)**").block();
		}


	}

	@Override
	public String getDescription() {
		return "Add a scheduled command.";
	}

	@Override
	public String getUsage() {
		return "schedule <command> <time>";
	}
}
