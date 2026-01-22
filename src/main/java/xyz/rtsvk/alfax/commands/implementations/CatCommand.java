package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Instant;
import java.util.List;


public class CatCommand implements ICommand {

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) {
		try {
			String imageUrl = "https://kocicividea.cz/macka?t=" + System.currentTimeMillis();
			EmbedCreateSpec table = EmbedCreateSpec.builder()
				.author("kocicividea.cz", "https://kocicividea.cz/", "")
				.title(language.getMessage("command.cat.embed-title"))
				.image(imageUrl)
				.timestamp(Instant.now())
				.build();

			chat.sendMessage(table);
		} catch (Exception e) {
			chat.sendMessage(language.getMessage("command.cat.out-of-cats"));
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public String getDescription() {
		return "command.cat.description";
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
