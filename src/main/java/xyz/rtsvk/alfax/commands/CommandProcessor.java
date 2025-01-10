package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.service.ApplicationService;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildStateRegister;
import xyz.rtsvk.alfax.util.ratelimit.*;
import xyz.rtsvk.alfax.util.statemachine.Predicates;
import xyz.rtsvk.alfax.util.statemachine.Lexeme;
import xyz.rtsvk.alfax.util.statemachine.Inputs;
import xyz.rtsvk.alfax.util.statemachine.lexer.CommandLexer;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.*;

import static xyz.rtsvk.alfax.util.text.TextUtils.QUOTES;

/**
 * Class for registration and processing of commands
 * @author Jastrobaron
 */
public class CommandProcessor {

	/** Command argument separator */
	public static final Character SEPARATOR = ' ';
	/** Logger for this class */
	private static final Logger logger = new Logger(CommandProcessor.class);

	/** Map for storing registered command-command data pairs. Command data is null for legacy commands. */
	private final Map<ICommand, ApplicationCommandData> commands;
	/** List containing the command executors that are currently running */
	private final List<Thread> runningCommandExecutors;
	/** Discord client providing access to the Discord database */
	private final GatewayDiscordClient gateway;
	/** Startup configuration */
	private final Config config;
	/** Semaphore for rate limiting. If a rate limit is exceeded, a {@link RateLimitExceededException} is thrown*/
	private final RateLimiter rateLimiter;
	/** Application ID */
	private final long appId;
	/** Fallback command to execute when the user enters an unknown command. */
	private ICommand fallback;

	/**
	 * Constructor
	 * @param gateway to access the Discord API
	 */
	public CommandProcessor(GatewayDiscordClient gateway, Config config) {
		this.gateway = gateway;
		this.config = config;
		this.commands = new HashMap<>();
		this.runningCommandExecutors = new ArrayList<>();
		this.appId = this.gateway.getRestClient().getApplicationId().blockOptional()
				.orElseThrow(() -> new IllegalStateException("Could not get application ID"));
		this.fallback = new CommandAdapter();
		this.rateLimiter = new RateLimiter(config.getInt("command-rate-limit"));
    }

	/**
	 * Get the command executor with the specified name.
	 * @param command name of the command
	 * @return the command executor or {@link #fallback} if the command is not found
	 */
	public ICommand getCommandExecutor(String command) {
		return this.getCommandExecutor(command, this.fallback);
	}

	/**
	 * Get the command executor with the specified name
	 * @param command name of the command
	 * @param fallback command executor to be returned when no command with the specified name is returned
	 * @return the command executor or the specified fallback, if the command is not found
	 */
	public ICommand getCommandExecutor(String command, ICommand fallback) {
		return this.commands.keySet().stream()
				.filter(cmd -> cmd.getName().equals(command) || cmd.getAliases().contains(command))
				.findFirst().orElse(fallback);
	}

	/**
	 * Run the command executor
	 * @param command name of the command
	 * @param user to run the command under
	 * @param chat to send the command output to
	 * @param args list of command arguments
	 * @param guildId ID of the guild the chat resides in
	 * @param language of the user
	 * @throws Exception if an error occurs
	 */
  	public void executeCommand(String command, User user, IChatContext chat, List<String> args, Snowflake guildId, MessageManager language) throws Exception {
		ICommand cmd = this.getCommandExecutor(command);
		GuildState guildState = GuildStateRegister.getGuildState(guildId);
		guildState.setLastCommandChat(chat);
		cmd.handle(user, chat, args, guildState, this.gateway, language);
	}

	/**
	 * Executes a command
	 * @param chat context to execute the command in
	 * @param commandString to execute
	 */
	public void executeCommand(IChatContext chat, String commandString) {
		Snowflake guildId = chat.getInvokerMessage().getGuildId().orElse(null);
		GuildState guildState = GuildStateRegister.getGuildState(guildId);
		if (guildState != null) {
			guildState.setLastCommandChat(chat);
		}

		User invoker = chat.getInvokerMessage().getAuthor().orElseThrow(IllegalStateException::new);
		MessageManager language = Database.getUserLanguage(invoker.getId());
		if (language == null) {
			logger.error(TextUtils.format("Failed to load language for user <@${0}>", invoker.getId().asString()));
			chat.sendMessage("A fatal error has occured. Please contact the administrator.");
			return;
		}

		List<String> splitCmdStr = splitCommandString(commandString);
		String cmdName = splitCmdStr.get(0);
		List<String> args = splitCmdStr.subList(1, splitCmdStr.size());
		ICommand executor = this.getCommandExecutor(cmdName);

		Thread cmdRunner = new Thread(() -> {
			try {
				this.rateLimiter.lock();
				executor.handle(invoker, chat, args, guildState, this.gateway, language);
			} catch (RateLimitExceededException ree) {
				chat.sendMessage(language.getMessage("general.error.rate-limit-exceeded"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				this.rateLimiter.unlock();
			}
		});
		cmdRunner.setName("CommandExecutor-" + cmdName + "-" + System.currentTimeMillis());
		this.runningCommandExecutors.add(cmdRunner);
		cmdRunner.start();
	}

	/**
	 * Register a new command executor
	 * @param command to register
     */
	public void registerCommand(ICommand command) {
		if (command instanceof IApplicationCommand appCmd) {
			ApplicationCommandData cmdData = this.gateway.getRestClient().getApplicationService()
					.createGlobalApplicationCommand(this.appId, appCmd.getCommandCreateRequest())
					.blockOptional().orElseThrow(IllegalStateException::new);
			this.commands.put(command, cmdData);
		} else {
			this.commands.put(command, null);
		}
	}

	/**
	 * Reload all the registered application commands.
	 */
	public void reloadApplicationCommands() {
		ApplicationService service = this.gateway.getRestClient().getApplicationService();
		this.commands.forEach((cmd, data) -> {
			if (cmd instanceof IApplicationCommand appCmd) {
				service.deleteGlobalApplicationCommand(this.appId, data.id().asLong());
				service.createGlobalApplicationCommand(this.appId, appCmd.getCommandCreateRequest());
			}
		});
	}

	/**
	 * @return list of registered commands
	 */
	public List<ICommand> getCommands() {
		return this.commands.keySet().stream().toList();
	}

	/**
	 * Set the fallback command executor
	 * @param command the command executor
	 */
	public void setFallback(ICommand command) {
		if (command == null) {
			throw new IllegalArgumentException("Fallback command executor cannot be set to null, please use CommandAdapter instead.");
		}
		this.fallback = command;
	}

	/**
	 * Split the command string. Anything within quotes is parsed as one argument.
	 * @param input to split
	 * @return list of strings to be then parsed as a command
	 */
	public static List<String> splitCommandString(String input) {
		CommandLexer fsm = new CommandLexer(SEPARATOR, QUOTES);
		fsm.setInput(Inputs.fromString(input));
		fsm.reset();

		// FIXME: This is just a workaround, because the state machine appends separators to the beginning of entries.
		return fsm.getAll().stream()
				.filter(Predicates.anyExcept(SEPARATOR))
				.map(Lexeme::value)
				.toList();
	}

	public void cleanup() {
		if (config.getBoolean("force-shutdown-on-exit")) {
			runningCommandExecutors.forEach(Thread::interrupt);
		} else { // wait for all command executors to finish
			while (runningCommandExecutors.stream().anyMatch(Thread::isAlive)) ;
		}
	}
}
