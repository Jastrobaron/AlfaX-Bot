package xyz.rtsvk.alfax.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.service.ApplicationService;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.impl.DiscordChat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.*;

/**
 * Class for registration and processing of commands
 * @author Jastrobaron
 */
public class CommandProcessor {

	/** Command argument separator */
	public static final Character SEPARATOR = ' ';
	/** List of characters that can be used as quotation marks */
	public static final List<Character> QUOTES = List.of('"', '\'');

	/** Map for storing registered command-command data pairs. Command data is null for legacy commands. */
	private final Map<ICommand, ApplicationCommandData> commands;
	/** Command prefix */
	private final String prefix;
	/** Discord client providing access to the Discord database */
	private final GatewayDiscordClient gateway;
	/** Application ID */
	private final long appId;
	/** Fallback command to execute when the user enters an unknown command. */
	private ICommand fallback = null;

	public CommandProcessor(GatewayDiscordClient gateway, String prefix) throws Exception {
		this.gateway = gateway;
		this.prefix = prefix;
		this.commands = new HashMap<>();
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

	public void executeCommand(String command, User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		ICommand cmd = this.getCommandExecutor(command, this.fallback);
		MessageManager language = Database.getUserLanguage(user.getId(), "legacy");
		try {
			cmd.handle(user, new DiscordChat(channel, messageId, this.prefix), args, guildId, bot, language);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public List<ICommand> getCommands() {
		return this.commands.keySet().stream().toList();
	}

	public ICommand getFallback() {
		return this.fallback;
	}

	public long getApplicationId() {
		return this.appId;
	}

	public static List<String> splitCommandString(String input) {
		List<String> output = new ArrayList<>();
		StringBuilder token = new StringBuilder();
		int state = 0;



		int index = 0;
		while (index < input.length()) {
			char currentChar = input.charAt(index++);
			switch (state) {
				case 0:
					if (currentChar == SEPARATOR) {
						state = 1;
					}
					else {
						token.append(currentChar);
					}
					break;

				case 1:
					if (!token.isEmpty()) {
						output.add(token.toString());
						token.setLength(0);
					}
					if (QUOTES.contains(currentChar)) {
						state = 2;
					}
					else {
						token.append(currentChar);
						state = 0;
					}
					break;

				case 2:
					if (currentChar == '\\') {
						state = 3;
					}
					else if (QUOTES.contains(currentChar)) {
						if (!token.isEmpty()) {
							output.add(token.toString());
							token.setLength(0);
						}
						state = 0;
					} else {
						token.append(currentChar);
					}
					break;

				case 3:
					token.append(currentChar);
					state = 2;
					break;
			}
		}

		if (!token.isEmpty()) {
			output.add(token.toString());
		}

		return output.stream().map(String::trim).toList();
	}
}
