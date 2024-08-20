package xyz.rtsvk.alfax.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.service.ApplicationService;
import xyz.rtsvk.alfax.util.chat.Chat;
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
	/** Stores guild state objects. Map structure is used for faster searching. */
	private final Map<Snowflake, GuildCommandState> guildStates;
	/** Discord client providing access to the Discord database */
	private final GatewayDiscordClient gateway;
	/** Audio player manager for musical functionality */
	private final AudioPlayerManager playerManager;
	/** Application ID */
	private final long appId;
	/** Fallback command to execute when the user enters an unknown command. */
	private ICommand fallback = null;

	public CommandProcessor(GatewayDiscordClient gateway, AudioPlayerManager playerManager) throws Exception {
		this.gateway = gateway;
		this.commands = new HashMap<>();
		this.guildStates = new HashMap<>();
		this.playerManager = playerManager;
		this.appId = this.gateway.getRestClient().getApplicationId().blockOptional()
				.orElseThrow(() -> new Exception("Could not get application ID"));
    }

	public ICommand getCommandExecutor(String command) {
		return this.getCommandExecutor(command, this.fallback);
	}

	public ICommand getCommandExecutor(String command, ICommand fallback) {
		return this.commands.keySet().stream()
				.filter(cmd -> cmd.getName().equals(command) || cmd.getAliases().contains(command))
				.findFirst().orElse(fallback);
	}

  public void executeCommand(String command, User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		ICommand cmd = this.getCommandExecutor(command);
		GuildCommandState guildState = this.getGuildState(guildId);
		cmd.handle(user, chat, args, guildState, bot, language);
	}

	public GuildCommandState getGuildState(Snowflake guildId) {
		if (guildId == null) {
			return null;
		}
		return this.guildStates.computeIfAbsent(guildId, id -> {
			AudioPlayer player = this.playerManager.createPlayer();
			return new GuildCommandState(id, player);
		});
	}

	public void registerCommand(ICommand command) throws Exception {
		if (command instanceof IApplicationCommand appCmd) {
			ApplicationCommandData cmdData = this.gateway.getRestClient().getApplicationService()
					.createGlobalApplicationCommand(this.appId, appCmd.getCommandCreateRequest())
					.blockOptional().orElseThrow(Exception::new);
			this.commands.put(command, cmdData);
		} else {
			this.commands.put(command, null);
		}
	}

	public void reloadApplicationCommands() {
		ApplicationService service = this.gateway.getRestClient().getApplicationService();
		this.commands.forEach((cmd, data) -> {
			if (cmd instanceof IApplicationCommand appCmd) {
				service.deleteGlobalApplicationCommand(this.appId, data.id().asLong());
				service.createGlobalApplicationCommand(this.appId, appCmd.getCommandCreateRequest());
			}
		});
	}

	public void setFallback(ICommand command) {
		this.fallback = command;
	}

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
