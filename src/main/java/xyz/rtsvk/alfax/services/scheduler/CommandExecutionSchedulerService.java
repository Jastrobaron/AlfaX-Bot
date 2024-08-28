package xyz.rtsvk.alfax.services.scheduler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.services.Service;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.chatcontext.impl.DiscordChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service to execute commands at a given date/time. Can be recurrent.
 * @author Jastrobaron
 */
// FIXME - Make this service work like it's supposed to
public class CommandExecutionSchedulerService extends Service {

	private GatewayDiscordClient gateway;
	private CommandProcessor proc;
	private Logger logger;
	private boolean executed;

	public CommandExecutionSchedulerService(GatewayDiscordClient gateway, CommandProcessor proc) {
		super("CommandExecutionScheduler");
		this.gateway = gateway;
		this.proc = proc;
		this.logger = new Logger(this.getClass());
		this.executed = false;
	}

	@Override
	protected void startup() throws Exception {
		// nothing to do here
	}

	@Override
	protected void loop() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		if (now.getMinute() % 5 != 0 && !this.executed) {
			return; // runs every 5 minutes
		}
		else if (now.getMinute() % 5 == 1) {
			this.executed = false; // reset the timer 1 minute after
		}

		List<Task> tasks = Database.getScheduleFor(now.toLocalDate());
		if (tasks.isEmpty()) {
			return;
		}

		this.logger.info("Got " + tasks.size() +  " tasks.");
		tasks.forEach(e -> {
			final List<String> commandArgs = new ArrayList<>(Arrays.asList(e.getCommand().split(" ")));
			String cmdName = commandArgs.remove(0);
			User self = this.gateway.getSelf().block();
			MessageChannel channel = (MessageChannel) this.gateway.getChannelById(e.getChannel()).block();
			IChatContext chat = new DiscordChatContext(channel, null, null);

			LocalDate execDate = e.getExecDate() != null ? e.getExecDate() : now.toLocalDate();
			LocalTime execTime = e.getExecTime() != null ? e.getExecTime() : now.toLocalTime();

			// day of week
			int day = now.toLocalDate().getDayOfWeek().getValue();

			if (now.toLocalTime() != execTime) return;
			if (now.toLocalDate() != execDate) return;

			if (e.getDays().isEmpty() || e.getDays().contains(String.valueOf(day))) {
				try {
					MessageManager language = MessageManager.getMessages("legacy");
					this.logger.info("Running command " +  cmdName);
					this.proc.executeCommand(cmdName, self, chat, commandArgs, e.getGuild(), language);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		});
		executed = true;
	}

	@Override
	protected void shutdown() throws Exception {
		// nothing to do here
	}
}
