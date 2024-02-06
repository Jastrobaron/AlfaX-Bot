package xyz.rtsvk.alfax;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.*;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.scheduler.CommandExecutionScheduler;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.tasks.TaskTimer;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.webserver.WebServer;

import java.util.*;

public class Main {
	public static void main(String[] args) throws Exception {

		final Config config = Config.from(args);
		final Logger logger = new Logger(Main.class);
		Logger.setLogFile(config.getStringOrDefault("log-file", "latest.log"));

		// to generate the default config, run the bot as `java -jar jarfile.jar --default-config`
		if (config.containsKey("default-config")) {
			String filename = config.getStringOrDefault("default-config", "config.properties.def");
			config.remove("default-config");
			Config.defaultConfig().forEach(config::putIfAbsent);
			config.write(filename);
			logger.info("Created default configuration file '" + filename + "'!");
			return;
		}

		// initialize database wrapper
		Database.init(
				config.getString("db-host"),
				config.getString("db-user"),
				config.getString("db-password"),
				config.getString("db-name")
		);

		int adminCount = Database.getAdminCount();
		if (adminCount == 0) {
			String token = config.getStringOrDefault("admin-token", getRandomString(128));
			logger.info("No admin users found! Admin token: " + token);
			config.putIfAbsent("admin-token", token);
		}
		else if (adminCount == -1) {
			logger.error("Failed to check admin users! Please check your database connection.");
			return;
		}

		// set up discord gateway
		final String prefix = config.getString("prefix");
		logger.info("Bot's prefix is " + prefix + " (length=" + prefix.length() + ")");
		final DiscordClient client = DiscordClient.create(config.getString("token"));
		final GatewayDiscordClient gateway = client.login().block();

		CommandProcessor proc = new CommandProcessor();
		proc.setFallback(new HelpCommand(proc));

		// register all commands
		proc.registerCommand(new HelpCommand(proc));
		proc.registerCommand(new TestCommand());
		proc.registerCommand(new FortuneTeller());
		proc.registerCommand(new PickCommand());
		proc.registerCommand(new TodayCommand());
		proc.registerCommand(new WeatherCommand(config));
		proc.registerCommand(new BigTextCommand());
		proc.registerCommand(new ChatGPTCommand(config));
		proc.registerCommand(new MqttPublishCommand(config));
		proc.registerCommand(new RegisterSensorCommand(prefix));
		proc.registerCommand(new CreateUserCommand());
		proc.registerCommand(new UserPermissionsCommand(config));
		proc.registerCommand(new RedeemAdminPermissionCommand(config));
		proc.registerCommand(new CreditsCommand());
		proc.registerCommand(new SetAnnouncementChannelCommand());
		proc.registerCommand(new ScheduleEventCommand());
		proc.registerCommand(new MathExpressionCommand());
		proc.registerCommand(new CatCommand());

		// scheduler
		if (config.getBooleanOrDefault("scheduler-enabled", false)) {
			Thread scheduler = new Thread(new CommandExecutionScheduler(gateway, proc));
			scheduler.start();
		}

		// webhook server
		if (config.containsKey("webserver-port")) {
			Thread webserver = new WebServer(config, gateway);
			webserver.start();
		}

		// MQTT Subscribe Client
		if (config.getBoolean("mqtt-enabled")) {
			Mqtt mqtt = new Mqtt(config, "AlfaX-Bot-Sub", gateway);
			mqtt.start();
		}

		// task timer
		TaskTimer timer = new TaskTimer(gateway, 1000);
		timer.setEnabled(true);

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				final Snowflake guildId = message.getGuildId().orElse(null);
				final MessageChannel channel = message.getChannel().block();
				final String msg = message.getContent().trim();
				final String mention = gateway.getSelf().block().getMention();

				if (user.isBot()) return;
				if (msg.startsWith(prefix)) {
					Thread cmdThread = new Thread(() -> {
						try {
							String cStr = message.getContent().substring(prefix.length());
							final List<String> commandArgs = new ArrayList<>(Arrays.asList(cStr.split(" ")));
							Command cmd = proc.getCommandExecutor(commandArgs.remove(0));

							if (cmd == null)
								channel.createMessage("**:question: Bracho, netusim co odomna chces. Napis '" + prefix + "help' pre zoznam prikazov. :thinking:**").block();
							else cmd.handle(user, channel, commandArgs, guildId, gateway);
						} catch (Exception e) {
							e.printStackTrace(System.out);
							channel.createMessage("**:x: " + e.getMessage() + "**").block();
						}
					});
					cmdThread.start();
				} else if (msg.startsWith(mention)) {
					Thread cmd = new Thread(() -> {
						try {
							Command c = proc.getCommandExecutor("gpt");
							List<String> tokens = Arrays.asList(msg.split(" "));
							c.handle(user, channel, tokens.subList(1, tokens.size()), guildId, gateway);
						} catch (Exception e) {
							e.printStackTrace(System.out);
							channel.createMessage("**:x: Nastala neocakavana chyba. Prosim, skontrolujte standardny vystup pre viac informacii.**").block();
						}
					});
					cmd.start();
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Database.close();
			logger.info("Shutting down...");
			gateway.logout().block();
			logger.info("Goodbye!");
		}));

		gateway.onDisconnect().block();
	}

	// create a function to generate a random string of length n
	public static String getRandomString(int n) {
		// chose a Character random from this String
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
									+ "0123456789"
									+ "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index = (int)(AlphaNumericString.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}
}
