package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class GetEmojiCommand implements ICommand {

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		Guild server = bot.getGuildById(guildState.getGuildId()).block();
		if (server == null) {
			chat.sendMessage(language.getMessage("command.get-emoji.error.server-not-found"));
			return;
		}

		if (args.isEmpty()) {
			chat.sendMessage(language.getFormattedString("command.usage").addParam("usage", this.getUsage()).addParam("prefix", chat.getCommandPrefix()).build());
			return;
		}

		String arg = args.get(0);
		String emoji;
		if (arg.startsWith("<:") && arg.endsWith(">")) {
			emoji = arg.substring(2, arg.length() - 1);
			server.getEmojis()
					.filter(e -> e.getId().equals(Snowflake.of(emoji.substring(emoji.indexOf(":") + 1))))
					.map(GuildEmoji::getImageUrl)
					.doOnNext(chat::sendMessage)
					.doOnError(e -> chat.sendMessage(language.getFormattedString("command.get-emoji.error").addParam("error", e.getMessage()).build()));
		} else {
			chat.sendMessage(language.getMessage("command.get-emoji.error.invalid-emoji-format"));
		}
	}

	@Override
	public String getName() {
		return "getemoji";
	}

	@Override
	public String getDescription() {
		return "command.get-emoji.description";
	}

	@Override
	public String getUsage() {
		return "getemoji <emoji>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("emoji");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
