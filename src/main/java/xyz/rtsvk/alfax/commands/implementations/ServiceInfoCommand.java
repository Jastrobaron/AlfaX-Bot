package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ServiceInfoCommand implements ICommand {

	private Supplier<Set<Thread>> threads;

	public ServiceInfoCommand(Supplier<Set<Thread>> threads) {
		this.threads = threads;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (!Database.checkPermissions(user.getId(), Database.PERMISSION_ADMIN))  {
			chat.sendMessage("Insufficient permissions!");
			return;
		}
		Set<Thread> currentThreads = this.threads.get();
		StringBuilder message = new StringBuilder();
		message.append("```");
		message.append("Service info:\n");
		message.append("Threads: " + currentThreads.size() + "\n");
		for (Thread thread : currentThreads) {
			message.append("  - " + thread.getName() + " (state: " + thread.getState() + ")\n");
		}
		message.append("```");
		chat.sendMessage(message.toString());
	}

	@Override
	public String getName() {
		return "service-info";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUsage() {
		return "service-info";
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
