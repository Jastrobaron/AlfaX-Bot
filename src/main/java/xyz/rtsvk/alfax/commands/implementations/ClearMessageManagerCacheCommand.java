package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.FormattedString;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class ClearMessageManagerCacheCommand implements ICommand {

	private Logger logger = new Logger(ClearMessageManagerCacheCommand.class);

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (!Database.checkPermissions(user.getId(), Database.PERMISSION_ADMIN)) {
			chat.sendMessage(language.getMessage("command.user.insufficient-permissions"));
			return;
		}
		MessageManager.clearCache();
		chat.sendMessage("Cache cleared!");
		this.logger.info(FormattedString.create("Message manager cache cleared by ${name} (id=${id})!")
				.addParam("name", user.getUsername())
				.addParam("id", user.getId().asString())
				.build());
	}

	@Override
	public String getName() {
		return "clear-message-manager-cache";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUsage() {
		return "clear-message-manager-cache";
	}

	@Override
	public List<String> getAliases() {
		return List.of("clear-msg-cache");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
