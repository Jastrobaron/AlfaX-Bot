package xyz.rtsvk.alfax.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public interface ICommand {

	/**
	 * Command executor method prototype
	 * @param user       user that queried the command execution
	 * @param chat       ID of the message containing the command execution query
	 * @param args       command arguments
	 * @param guildState state object of the guild
	 * @param bot        bot reference, just to interact with the Discord API if needed
	 * @param language   language manager to get localized messages
	 * @throws Exception if something goes wrong during command execution
	 */
	void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception;

	/**
	 * @return the command name
	 */
	String getName();

	/**
	 * @return the command description
	 */
	String getDescription();

	/**
	 * @return the command usage
	 */
	String getUsage();

	/**
	 * @return the command aliases
	 */
	List<String> getAliases();

	/**
	 * @return the command cooldown
	 */
	int getCooldown();
}
