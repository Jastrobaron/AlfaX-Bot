package xyz.rtsvk.alfax;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import xyz.rtsvk.alfax.commands.CommandAdapter;
import xyz.rtsvk.alfax.services.ServiceManager;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.services.mqtt.MqttService;
import xyz.rtsvk.alfax.reactions.IReactionCallback;
import xyz.rtsvk.alfax.reactions.ReactionCallbackRegister;
import xyz.rtsvk.alfax.reactions.impl.BookmarkReactionCallback;
import xyz.rtsvk.alfax.services.scheduler.CommandExecutionSchedulerService;
import xyz.rtsvk.alfax.tasks.TaskTimer;
import xyz.rtsvk.alfax.util.*;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.chatcontext.impl.DiscordChatContext;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.storage.FileManager;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;
import xyz.rtsvk.alfax.services.webserver.WebServerService;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main class of the bot
 */
public class Main {

	/**
	 * Main method
	 * @param args command line arguments
	 * @throws Exception if an error occurred
	 */
	public static void main(String[] args) throws Exception {

		final Config config = Config.fromCommandLineArgs(args);
		final Logger logger = new Logger(Main.class);
		FileManager.init();
		Logger.setLogFile(config.getStringOrDefault("log-file", "latest.log"));

		// if the bot is run with --copy-default-config, it will generate a default config file and exit
		if (config.containsKey("copy-default-config")) {
			String filename = config.getStringOrDefault("copy-default-config", "default-config.properties");
			Config.copyDefaultConfig(filename);
			logger.info("Default configuration saved to file '" + filename + "'!");
			return;
		}

		Config.defaultConfig().forEach(config::putIfAbsent);    // fill in missing values with defaults
		if (config.containsKey("save-config")) {
			String filename = config.getStringOrDefault("save-config", "saved-config_" + System.currentTimeMillis() + ".properties");
			config.remove("save-config");
			config.write(filename);
			logger.info("Current configuration saved to file '" + filename + "'!");
		}

		// initialize database wrapper
		int attempts = config.getInt("db-max-connection-retries");
		while (attempts > 0) {
			Database.init(config);
			if (Database.isInitialized()) {
				break;
			} else {
				attempts--;
				logger.warn(String.format("Failed to connect to the database server; retrying in 3s, remaining attempts: %d", attempts));
				Thread.sleep(3000);
			}
		}

		if (attempts == 0) {
			logger.error("Failed to connect to the database server; is it running?");
			return;
		}

		int adminCount = Database.getAdminCount();
		if (adminCount == 0) {
			String token = config.getStringOrDefault("admin-token", TextUtils.getRandomString(128));
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
		final GatewayDiscordClient gateway = client.login().blockOptional().orElseThrow(IllegalStateException::new);
		final User self = gateway.getSelf().blockOptional().orElseThrow(IllegalStateException::new);
		final String botMention = self.getMention();

		MessageManager.setDefaultLanguage(config.getString("default-language"));
		MessageManager.setForceDefaultLanguage(config.getBoolean("force-default-language"));
		CommandProcessor proc = new CommandProcessor(gateway, config);
		proc.setFallback(new CommandAdapter() {
			@Override
			public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) {
				chat.sendMessage(language.getFormattedString("command.not-found")
						.addParam("prefix", prefix).build());
			}
		});

		// register all commands
		proc.registerCommand(new HelpCommand(proc));
		proc.registerCommand(new TestCommand());
		proc.registerCommand(new FortuneTellerCommand());
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
		proc.registerCommand(new CreditsCommand(config));
		proc.registerCommand(new SetAnnouncementChannelCommand());
		proc.registerCommand(new ScheduleEventCommand());
		proc.registerCommand(new MathExpressionCommand());
		proc.registerCommand(new CatCommand());
		proc.registerCommand(new TextToSpeechCommand(config));
		proc.registerCommand(new GenerateImageCommand(config));
		proc.registerCommand(new RollDiceCommand());
		proc.registerCommand(new CreditBuyCommand());
		proc.registerCommand(new MeCommand());
		proc.registerCommand(new PollCreateCommand());
		proc.registerCommand(new PollEndCommand());
		proc.registerCommand(new SetLanguageCommand());
		proc.registerCommand(new ClearMessageManagerCacheCommand());
		proc.registerCommand(new ServiceInfoCommand(() -> Thread.getAllStackTraces().keySet()));
		proc.registerCommand(new GetEmojiCommand());
		proc.registerCommand(new PlayCommand());
		proc.registerCommand(new PauseCommand());
		proc.registerCommand(new JoinVoiceCommand());
		proc.registerCommand(new SkipCommand());
		proc.registerCommand(new LeaveCommand());
		proc.registerCommand(new MusicQueueCommand());
		proc.registerCommand(new SkipAllCommand());

		ServiceManager serviceMgr = new ServiceManager();
		if (config.getBoolean("scheduler-enabled")) {		// scheduler
			serviceMgr.addService(() -> new CommandExecutionSchedulerService(gateway, proc));
		}
		if (config.getBoolean("webserver-enabled")) {		// webhook server
			serviceMgr.addService(() -> new WebServerService(config, gateway));
		}
		if (config.getBoolean("mqtt-enabled")) { 			// MQTT Subscribe Client
			serviceMgr.addService(() -> new MqttService(config, gateway));
		}
		serviceMgr.start();

		// task timer
		TaskTimer timer = new TaskTimer(gateway, 1000);
		timer.setEnabled(true);

		final String commandOnTag = config.getString("command-on-tag");
		logger.info("Command on tag: " + commandOnTag);
		Map<Snowflake, List<String>> lastMessageCount = new HashMap<>();
		boolean spammerEnabled = config.getBoolean("spammer-enabled");
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				if (user.isBot()) return;

				final MessageChannel channel = message.getChannel().blockOptional().orElseThrow(IllegalStateException::new);
				final String msg = message.getContent().trim();

				if (spammerEnabled) {
					if (lastMessageCount.containsKey(channel.getId())) {
						List<String> lastMessages = lastMessageCount.get(channel.getId());
						logger.info("Last message count: " + lastMessages.size() + " (channel=" + channel.getId().asString() + ")");
						lastMessages.add(msg);
						if (lastMessages.stream().allMatch(lm -> lm.equals(msg)) && lastMessages.size() >= 3) {
							channel.createMessage(msg).block();
							lastMessages.clear();
						}
					} else {
						lastMessageCount.put(channel.getId(), new ArrayList<>(List.of(msg)));
					}
				}

				String command;
				if (msg.startsWith(botMention)) {
					command = msg.replace(botMention, commandOnTag);
				} else if (msg.startsWith(prefix)) {
					command = msg.substring(prefix.length());
				} else {
					return;
				}

				IChatContext chat = new DiscordChatContext(channel, message, prefix);
				String messageToLog = chat.isPrivate()
						? TextUtils.format("<private message of length ${0}>", command.length())
						: command;
				logger.info(TextUtils.format("Command received: ${0} (user=${1}, channel=${2})", messageToLog, user.getUsername(), channel.getId().asString()));
				try {
					proc.executeCommand(chat, command);
				}  catch (Exception e) {
					e.printStackTrace(System.out);
					chat.sendMessage("**:x: " + e.getMessage() + "**");
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		ReactionCallbackRegister rce = new ReactionCallbackRegister();
		rce.addReactionCallback(new BookmarkReactionCallback());

		gateway.on(ReactionAddEvent.class).subscribe(event -> {
			try {
				ReactionEmoji emoji = event.getEmoji();
				Optional<IReactionCallback> cb = rce.getReactionCallback(emoji);
				if (cb.isEmpty()) {
					return;
				}

				Message message = event.getMessage().blockOptional().orElseThrow(IllegalStateException::new);
				User user = event.getUser().blockOptional().orElseThrow(IllegalStateException::new);
				MessageManager language = Database.getUserLanguage(user.getId());
				long reactionCount = message.getReactions().stream()
						.filter(r -> r.getEmoji().equals(emoji))
						.count();
				cb.get().handle(message, user, language, reactionCount);
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		AtomicBoolean shutdownRequested = new AtomicBoolean(false);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdownRequested.set(true);
			serviceMgr.interrupt();
			proc.cleanup();
			timer.setEnabled(false);
			Database.close();
			FileManager.close();
			gateway.logout().block();
			logger.info("Goodbye!");
		}));

		gateway.onDisconnect().subscribe(event -> {
			if (shutdownRequested.get()) return;
			logger.error("Disconnected from Discord! Shutting down...");
			Runtime.getRuntime().exit(1);
		});

		logger.info("Bot is ready!");
	}
}