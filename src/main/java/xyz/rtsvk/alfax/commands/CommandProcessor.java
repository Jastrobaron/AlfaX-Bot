package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.*;

public class CommandProcessor {

	private final List<Command> cmds = new ArrayList<>();
	private Command fallback = null;

	public Command getCommandExecutor(String command) {
		return this.getCommandExecutor(command, this.fallback);
	}

	public Command getCommandExecutor(String command, Command fallback) {
		return this.cmds.stream()
				.filter(cmd -> cmd.getName().equals(command) || cmd.getAliases().contains(command))
				.findFirst()
				.orElse(fallback);
	}

	public void executeCommand(String command, User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		Command cmd = this.getCommandExecutor(command);
		if (cmd == null) {
			cmd = this.fallback;
		}
		try {
			cmd.handle(user, messageId, channel, args, guildId, bot);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerCommand(Command command) {
		this.cmds.add(command);
	}

	public void setFallback(Command command) {
		this.fallback = command;
	}

	public List<Command> getCommands() {
		return cmds;
	}
	public Command getFallback() {
		return this.fallback;
	}
}
