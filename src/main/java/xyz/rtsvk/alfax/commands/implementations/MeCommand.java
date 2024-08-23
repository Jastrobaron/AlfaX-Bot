package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Instant;
import java.util.List;

public class MeCommand implements ICommand {
	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String name = guildState == null ? user.getUsername() : user.asMember(guildState.getGuildId()).block().getDisplayName();
		EmbedCreateSpec table = EmbedCreateSpec.builder()
				.title(name)
				.addField(language.getMessage("command.me.field-id"), user.getId().asString(), false)
				.image(user.getAvatarUrl())
				.addField(language.getMessage("command.me.field-credits"), String.valueOf(Database.getUserCredits(user.getId())), false)
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
		return "command.me.description";
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
