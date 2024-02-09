package xyz.rtsvk.alfax;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.AudioProvider;
import xyz.rtsvk.alfax.commands.*;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.scheduler.CommandExecutionScheduler;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.tasks.TaskTimer;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.lavaplayer.LavaPlayerAudioProvider;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;
import xyz.rtsvk.alfax.webserver.WebServer;

import javax.sound.midi.Track;
import java.util.*;

public class Main {
	public static void main(String[] args) throws Exception {

		final Config config = Config.from(args);
		final Logger logger = new Logger(Main.class);
		Logger.setLogFile(config.getStringOrDefault("log-file", "latest.log"));
		Config.defaultConfig().forEach(config::putIfAbsent);

		// to generate the default config, run the bot as `java -jar jarfile.jar --default-config`
		if (config.containsKey("default-config")) {
			String filename = config.getStringOrDefault("default-config", "config.properties.def");
			config.remove("default-config");
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

		final Map<Snowflake, Queue<Track>> queues = new HashMap<>();
		final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		//playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		AudioSourceManagers.registerRemoteSources(playerManager);
		final AudioPlayer player = playerManager.createPlayer();
		AudioProvider provider = new LavaPlayerAudioProvider(player);
		TrackScheduler trackScheduler = new TrackScheduler(player);
		player.addListener(trackScheduler);

		// set up discord gateway
		final String prefix = config.getString("prefix");
		logger.info("Bot's prefix is " + prefix + " (length=" + prefix.length() + ")");
		final DiscordClient client = DiscordClient.create(config.getString("token"));
		final GatewayDiscordClient gateway = client.login().blockOptional().orElseThrow(Exception::new);
		final User self = gateway.getSelf().blockOptional().orElseThrow(Exception::new);
		final String botMention = self.getMention();

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
		//proc.registerCommand(new PlayCommand(playerManager, player, provider, trackScheduler));

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

		List<Thread> runningCommandExecutors = new ArrayList<>();
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				if (user.isBot()) return;

				final String msg = message.getContent().trim();
				final List<String> tokenList = new ArrayList<>(Arrays.asList(msg.split(" ")));
				final String first = tokenList.get(0);
				if (!first.equals(botMention) && !first.startsWith(prefix)) return;

				final Snowflake guildId = message.getGuildId().orElse(null);
				final MessageChannel channel = message.getChannel().block();

				Thread cmdThread = new Thread(() -> {
					try {
						String cmdName = first.startsWith(prefix) ? first.substring(prefix.length()) : "chatgpt";
						Command cmd = proc.getCommandExecutor(cmdName);
						if (cmd == null)
							channel.createMessage("**:question: Bracho, netusim co odomna chces. Napis '" + prefix + "help' pre zoznam prikazov. :thinking:**").block();
						else cmd.handle(user, channel, tokenList.subList(1, tokenList.size()), guildId, gateway);

					} catch (Exception e) {
						e.printStackTrace(System.out);
						channel.createMessage("**:x: " + e.getMessage() + "**").block();
					}
				});
				cmdThread.start();
				runningCommandExecutors.add(cmdThread);


			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			runningCommandExecutors.forEach(Thread::interrupt);
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
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
									+ "0123456789"
									+ "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			// generate a random number between
			// 0 to alphabet  variable length
			int index = (int)(alphabet.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(alphabet.charAt(index));
		}

		return sb.toString();
	}
}