package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.service.ApplicationService;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildStateRegister;
import xyz.rtsvk.alfax.util.statemachine.Predicates;
import xyz.rtsvk.alfax.util.statemachine.lex.SeparatedValuesStateMachine;
import xyz.rtsvk.alfax.util.statemachine.lex.StringBufferStateMachine;
import xyz.rtsvk.alfax.util.statemachine.input.InputSuppliers;
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

	/** Map for storing registered command-command data pairs. Command data is null for legacy commands. */
	private final Map<ICommand, ApplicationCommandData> commands;
	/** Discord client providing access to the Discord database */
	private final GatewayDiscordClient gateway;
	/** Fallback command to execute when the user enters an unknown command. */
	private ICommand fallback = null;
	/** Application ID */
	private final long appId;

	/**
	 * Constructor
	 * @param gateway to access the Discord API
	 */
	public CommandProcessor(GatewayDiscordClient gateway) {
		this.commands = new HashMap<>();
		this.gateway = gateway;
		this.appId = this.gateway.getRestClient().getApplicationId().blockOptional()
				.orElseThrow(() -> new IllegalStateException("Could not get application ID"));
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
		this.fallback = command;
	}

	/**
	 * Split the command string. Anything within quotes is parsed as one argument.
	 * @param input to split
	 * @return list of strings to be then parsed as a command
	 */
	public static List<String> splitCommandString(String input) {
		StringBufferStateMachine fsm = new SeparatedValuesStateMachine(SEPARATOR, QUOTES);
		fsm.setInputSupplier(InputSuppliers.fromString(input));
		fsm.reset();

		// FIXME: This is just a workaround, because the state machine appends separators to the beginning of entries.
		return fsm.getAll().stream()
				.filter(Predicates.anyStringExcept(SEPARATOR.toString()))
				.map(String::trim)
				.map(TextUtils::removeQuotes)
				.toList();
	}
}
