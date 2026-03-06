package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;

public class ScheduleEventCommand implements Command {

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		// syntax: schedule <event name> <event time> <event description>
		if (args.size() < 3) {
			channel.createMessage("Syntax: schedule <event name> <event time> <event description>").block();
			return;
		}

		ZonedDateTime t = LocalDateTime.parse(args.get(2)).atZone(ZoneId.systemDefault());

		String name = args.get(1);
		String time = String.valueOf(t.toInstant().toEpochMilli()); // throws exception if invalid
		String description = String.join(" ", args.subList(3, args.size()));

		Database.addEvent(name, description, time, guildId);
		channel.createMessage("Udalost pridana!").block();
	}

	@Override
	public String getName() {
		return "schedule";
	}

	@Override
	public String getDescription() {
		return "Schedule an event to be reminded of.";
	}

	@Override
	public String getUsage() {
		return "schedule <event name> <event time> <event description>";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}
}
