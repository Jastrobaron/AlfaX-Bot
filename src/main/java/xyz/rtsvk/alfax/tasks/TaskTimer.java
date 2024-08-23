package xyz.rtsvk.alfax.tasks;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class TaskTimer extends Thread {

	private GatewayDiscordClient client;
	private Logger logger;
	private long last, interval;
	private boolean running;

	private static final long REMIND_5MIN = 5 * 60 * 1000;
	private static final long REMIND_10MIN = 10 * 60 * 1000;
	private static final long REMIND_15MIN = 15 * 60 * 1000;
	private static final long REMIND_30MIN = 30 * 60 * 1000;
	private static final long REMIND_1HOUR = 60 * 60 * 1000;
	private static final long[] REMINDS = {REMIND_5MIN, REMIND_10MIN, REMIND_15MIN, REMIND_30MIN, REMIND_1HOUR};

	public TaskTimer(GatewayDiscordClient client, long interval) {
		this.last = System.currentTimeMillis();
		this.setName("TaskTimer");
		this.client = client;
		this.logger = new Logger(this.getClass());
		this.interval = interval;
		this.running = true;
	}

	@Override
	public void run() {

		while (this.running) {
			long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			if (now - this.last >= this.interval) {

				List<Event> events = Database.getEvents();
				if (events == null) {
					throw new RuntimeException("Failed to get events from database!");
				}

				for (Event event : events) {
					if (event.isReminded()) continue;

					Snowflake announcementChannel = Database.getAnnouncementChannel(Snowflake.of(event.getGuildID()));
					long time = event.getTime();
					long diff = time - now;
					if (diff <= 0) {
						// send reminder
						MessageChannel channel = (MessageChannel) client.getChannelById(announcementChannel).block();
						if (channel != null) {
							channel.createMessage("Udalost " + event.getName() + " sa zacina!").block();
						}
						event.setReminded(true);
					}
					else {
						// check if reminder should be sent
						for (long remind : REMINDS) {
							if (diff <= remind) {
								MessageChannel channel = (MessageChannel) client.getChannelById(announcementChannel).block();
								if (channel != null) {
									channel.createMessage("Udalost " + event.getName() + " sa zacina za " + (remind / 1000 / 60) + " minut!").block();
								}
								break;
							}
						}
					}
				}

				this.last = now;
			}
		}
	}

	public void setEnabled(boolean enabled) {
		this.running = enabled;
		if (this.running) {
			this.last = System.currentTimeMillis();
			this.start();
		}
	}

}
