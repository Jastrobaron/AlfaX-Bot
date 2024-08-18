package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class SetAnnouncementChannelCommand implements ICommand {

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_ADMIN)) {
			chat.sendMessage("You don't have permissions to do that");
			return;
		}

		// syntax: ac <channel id>
		if (args.isEmpty()) {
			chat.sendMessage("Syntax: setannouncementchannel <channel id>");
			return;
		}

		String channelID = args.get(0);
		if (channelID.isEmpty()) {
			chat.sendMessage("Channel ID is empty");
			return;
		}

		Database.setAnnouncementChannel(guildState.getGuildId(), Snowflake.of(channelID));
		chat.sendMessage("Kanal pre oznamenia bol nastaveny na <#" + channelID + ">");
	}

	@Override
	public String getName() {
		return "setannouncementchannel";
	}

	@Override
	public String getDescription() {
		return "Set the channel where the bot will send announcements.";
	}

	@Override
	public String getUsage() {
		return "ac <channel id>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("ac");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
