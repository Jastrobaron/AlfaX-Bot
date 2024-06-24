package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Instant;
import java.util.List;

public class MeCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String name = guildId == null ? user.getUsername() : user.asMember(guildId).block().getDisplayName();
		EmbedCreateSpec table = EmbedCreateSpec.builder()
				.title(name)
				.addField("ID:", user.getId().asString(), false)
				.image(user.getAvatarUrl())
				.addField("Kredity:", String.valueOf(Database.getUserCredits(user.getId())), false)
				.timestamp(Instant.now())
				.build();
		chat.sendMessage(table);
	}

	@Override
	public String getName() {
		return "me";
	}

	@Override
	public String getDescription() {
		return "Zobrazí informácie o tebe";
	}

	@Override
	public String getUsage() {
		return "me";
	}

	@Override
	public List<String> getAliases() {
		return List.of("i");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
