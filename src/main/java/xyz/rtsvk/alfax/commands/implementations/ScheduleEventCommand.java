package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class ScheduleEventCommand implements ICommand {

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		// syntax: schedule <event name> <event time> <event description>
		if (args.size() < 3) {
			chat.sendMessage("Syntax: schedule <event name> <event time> <event description>");
			return;
		}

		ZonedDateTime t = LocalDateTime.parse(args.get(2)).atZone(ZoneId.systemDefault());

		String name = args.get(1);
		String time = String.valueOf(t.toInstant().toEpochMilli()); // throws exception if invalid
		String description = String.join(" ", args.subList(3, args.size()));

		Database.addEvent(name, description, time, guildState.getGuildId());
		chat.sendMessage("Udalost pridana!");
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

	@Override
	public int getCooldown() {
		return 0;
	}
}
