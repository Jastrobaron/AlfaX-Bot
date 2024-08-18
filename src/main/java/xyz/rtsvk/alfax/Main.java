package xyz.rtsvk.alfax;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import xyz.rtsvk.alfax.commands.CommandAdapter;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.commands.implementations.*;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.reactions.IReactionCallback;
import xyz.rtsvk.alfax.reactions.ReactionCallbackRegister;
import xyz.rtsvk.alfax.reactions.impl.BookmarkReactionCallback;
import xyz.rtsvk.alfax.scheduler.CommandExecutionScheduler;
import xyz.rtsvk.alfax.tasks.TaskTimer;
import xyz.rtsvk.alfax.util.*;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.chat.impl.DiscordChat;
import xyz.rtsvk.alfax.util.ratelimit.RateLimitExceededException;
import xyz.rtsvk.alfax.util.ratelimit.RateLimiter;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;
import xyz.rtsvk.alfax.webserver.WebServer;

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
		Database.init(config);

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
		final GatewayDiscordClient gateway = client.login().blockOptional().orElseThrow(Exception::new);
		final User self = gateway.getSelf().blockOptional().orElseThrow(Exception::new);
		final String botMention = self.getMention();
		final String defaultLanguage = config.getString("default-language");
		final boolean forceDefaultLanguage = config.getBoolean("force-default-language");

		AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);

		CommandProcessor proc = new CommandProcessor(gateway, playerManager);
		proc.setFallback(new CommandAdapter() {
			@Override
			public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) {
				chat.sendMessage(language.getFormattedString("command.not-found").addParam("prefix", prefix).build());
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

		ServiceWatcher watcher = new ServiceWatcher();
		Thread scheduler = new CommandExecutionScheduler(gateway, proc);
		Thread webserver = new WebServer(config, gateway);
		Mqtt mqtt = new Mqtt(config, gateway);

		// scheduler
		if (config.getBoolean("scheduler-enabled")) {
			scheduler.setUncaughtExceptionHandler(watcher);
			scheduler.start();
		}

		// webhook server
		if (config.getBoolean("webserver-enabled")) {
			webserver.setUncaughtExceptionHandler(watcher);
			webserver.start();
		}

		// MQTT Subscribe Client
		if (config.getBoolean("mqtt-enabled")) {
			mqtt.setUncaughtExceptionHandler(watcher);
			mqtt.start();
		}

		// task timer
		TaskTimer timer = new TaskTimer(gateway, 1000);
		timer.setEnabled(true);

		final String commandOnTag = config.getString("command-on-tag");
		logger.info("Command on tag: " + commandOnTag);
		List<Thread> runningCommandExecutors = new ArrayList<>();
		Map<Snowflake, List<String>> lastMessageCount = new HashMap<>();
		boolean spammerEnabled = config.getBoolean("spammer-enabled");
		RateLimiter commandRatelimiter = new RateLimiter(config.getInt("command-rate-limit"));
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			try {
				final Message message = event.getMessage();
				final User user = message.getAuthor().orElseThrow(Exception::new);
				if (user.isBot()) return;

				final Snowflake guildId = message.getGuildId().orElse(null);
				final MessageChannel channel = message.getChannel().block();
				assert channel != null;
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

				if (!msg.startsWith(botMention) && !msg.startsWith(prefix)) return;
				final List<String> tokenList = CommandProcessor.splitCommandString(msg);
				final String first = tokenList.get(0);
				MessageManager language = forceDefaultLanguage
						? MessageManager.getMessages(defaultLanguage)
						: Database.getUserLanguage(user.getId(), defaultLanguage);
				Chat chat = new DiscordChat(channel, message.getId(), prefix);

				if (language == null) {
					logger.error(TextUtils.format("Failed to load language for user <@${0}>", user.getId().asString()));
					chat.sendMessage("A fatal error has occured. Please contact the administrator.");
					return;
				}

				String cmdName = first.startsWith(prefix) ? first.substring(prefix.length()) : commandOnTag;
				String messageToLog = (channel instanceof PrivateChannel)
						? TextUtils.format("<private message of length ${0}>", msg.length())
						: msg;
				logger.info(TextUtils.format("Command received: ${0} (user=${1}, channel=${2})", messageToLog, user.getUsername(), channel.getId().asString()));
				Thread cmdThread = new Thread(() -> {
					try {
						commandRatelimiter.lock();
						proc.executeCommand(cmdName, user, chat, tokenList.subList(1, tokenList.size()), guildId, gateway, language);
					} catch (RateLimitExceededException ree) {
						chat.sendMessage(language.getMessage("general.error.rate-limit-exceeded"));
					} catch (Exception e) {
						e.printStackTrace(System.out);
						chat.sendMessage("**:x: " + e.getMessage() + "**");
					} finally {
						commandRatelimiter.unlock();
					}
				});
				cmdThread.start();
				cmdThread.setName("CommandExecutor-" + cmdName + "-" + System.currentTimeMillis());
				runningCommandExecutors.add(cmdThread);
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

				Optional<Message> messageOpt = event.getMessage().blockOptional();
				Optional<User> userOpt = event.getUser().blockOptional();
				if (messageOpt.isPresent() && userOpt.isPresent()) {
					Message message = messageOpt.get();
					User user = userOpt.get();
					MessageManager language = forceDefaultLanguage
							? MessageManager.getMessages(defaultLanguage)
							: Database.getUserLanguage(user.getId(), defaultLanguage);
					long reactionCount = message.getReactions().stream()
							.filter(r -> r.getEmoji().equals(emoji))
							.count();
					cb.get().handle(message, user, language, reactionCount);
				}
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});

		AtomicBoolean shutdownRequested = new AtomicBoolean(false);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdownRequested.set(true);
			if (scheduler.isAlive()) {
				scheduler.interrupt();
			}
			if (webserver.isAlive()) {
				webserver.interrupt();
			}
			if (mqtt.isAlive()) {
				mqtt.interrupt();
			}
			timer.setEnabled(false);
			if (config.getBoolean("force-shutdown-on-exit")) {
				runningCommandExecutors.forEach(Thread::interrupt);
			}
			else { // wait for all command executors to finish
				while (runningCommandExecutors.stream().anyMatch(Thread::isAlive));
			}
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
