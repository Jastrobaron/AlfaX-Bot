package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetEmojiCommand implements ICommand {

	private static final Pattern EMOJI_STRING_REGEX = Pattern.compile("<:[a-zA-z]+:[0-9]+>");
	private static final Pattern EMOJI_ID_REGEX = Pattern.compile(":(\\d+)>");

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (args.isEmpty()) {
			chat.sendMessage(language.getFormattedString("command.usage").addParam("usage", this.getUsage()).addParam("prefix", chat.getCommandPrefix()).build());
			return;
		}

		String arg = args.get(0);
		if (!EMOJI_STRING_REGEX.matcher(arg).matches()) {
			chat.sendMessage(language.getMessage("command.get-emoji.error.invalid-emoji-format"));
			return;
		}

		Matcher idMatcher = EMOJI_ID_REGEX.matcher(arg);
		if (idMatcher.find()) {
			String emojiID = idMatcher.group(1);
			chat.sendMessage(TextUtils.format("https://cdn.discordapp.com/emojis/${0}.png", emojiID));
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
