package xyz.rtsvk.alfax.scheduler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.util.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandExecutionScheduler extends Thread {

	private final GatewayDiscordClient gateway;
	private final CommandProcessor proc;
	private final Logger logger;
	private boolean executed;

	public CommandExecutionScheduler(GatewayDiscordClient gateway, CommandProcessor proc) {
		super("CommandExecutionScheduler");
		this.gateway = gateway;
		this.proc = proc;
		this.logger = new Logger(this.getClass());
		this.executed = false;
	}

	@Override
	public void run() {
		while (true) {
			LocalDateTime now = LocalDateTime.now();
			if (now.getMinute() % 5 != 0 && !this.executed) continue; // runs every 5 minutes
			else if (now.getMinute() % 5 == 1) this.executed = false; // reset the timer 1 minute after

			List<Task> tasks = Database.getScheduleFor(now.toLocalDate());
			this.logger.info("Got " + tasks.size() +  " tasks.");

			if (!tasks.isEmpty()) tasks.forEach(e -> {
				final List<String> commandArgs = new ArrayList<>(Arrays.asList(e.getCommand().split(" ")));
				String cmdName = commandArgs.remove(0);
				Command cmd = proc.getCommandExecutor(cmdName);
				MessageChannel channel = (MessageChannel) this.gateway.getChannelById(e.getChannel()).block();

				LocalDate execDate = e.getExecDate() != null ? e.getExecDate() : now.toLocalDate();
				LocalTime execTime = e.getExecTime() != null ? e.getExecTime() : now.toLocalTime();

				// day of week
				int day = now.toLocalDate().getDayOfWeek().getValue();

				if (cmd == null) return;
				if (now.toLocalTime() != execTime) return;
				if (now.toLocalDate() != execDate) return;

				if (implicate(!e.getDays().isEmpty(), e.getDays().contains(String.valueOf(day)))) {
					try {
						this.logger.info("Running command " +  cmdName);
						cmd.handle(this.gateway.getSelf().block(), null, channel, commandArgs, e.getGuild(), this.gateway);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			});
			executed = true;
		}
	}

	private static boolean implicate(boolean a, boolean b) {
		return !a || b;
	}
}
