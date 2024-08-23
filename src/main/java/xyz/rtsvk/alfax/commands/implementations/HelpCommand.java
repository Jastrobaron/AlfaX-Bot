package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.commands.CommandProcessor;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.FormattedString;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class HelpCommand implements ICommand {

	/**
	 * Command processor
	 */
	private final CommandProcessor processor;

	/**
	 * Class constructor
	 * @param processor command processor
	 */
	public HelpCommand(CommandProcessor processor) {
		this.processor = processor;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) {
		StringBuilder sb = new StringBuilder();

		if (args.isEmpty()) {
			sb.append(language.getMessage("command.help.label.commands") + "```\n");
			final List<ICommand> cmds = this.processor.getCommands();
			cmds.forEach(e -> {
				String desc = language.getMessage(e.getDescription());
				if (desc == null) return;                   // if the command does not have a description, hide it
				sb.append(e.getName()).append(" - ").append(desc).append('\n');
			});
			sb.append("```");
			chat.sendMessage(sb.toString());
		}
		else {
			String cmdName = args.get(0);
			ICommand command = this.processor.getCommandExecutor(cmdName);
			if (command == null) {
				chat.sendMessage(language.getFormattedString("command.help.error.command-not-found").addParam("command", cmdName).build());
				return;
			}
			sb.append("```\n${usage}: ").append(command.getUsage()).append('\n');
			sb.append("${description}: ").append(command.getDescription()).append('\n');
			if (!command.getAliases().isEmpty()) {
				sb.append("${aliases}: ").append(String.join(", ", command.getAliases()));
			}
			sb.append("\n```");
			chat.sendMessage(FormattedString.create(sb.toString())
					.addParam("usage", language.getMessage("command.help.label.usage"))
					.addParam("description", language.getMessage("command.help.label.description"))
					.addParam("aliases", language.getMessage("command.help.label.aliases"))
					.build());
		}
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "command.help.description";
	}

	@Override
	public String getUsage() {
		return "help [prikaz]";
	}

	@Override
	public List<String> getAliases() {
		return List.of("manual");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
