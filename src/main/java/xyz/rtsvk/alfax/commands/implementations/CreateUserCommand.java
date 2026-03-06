package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.TextUtils;

import java.util.List;
import java.util.Properties;

public class CreateUserCommand implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		String userId = user.getId().asString();
		String hash = TextUtils.hash(userId + System.currentTimeMillis() + Math.random());
		Database.addUser(userId, hash, Database.PERMISSION_NONE);
		PrivateChannel dm = user.getPrivateChannel().block();
		if (dm != null) {
			channel.createMessage("Tvoj API token bol vytvoreny, pozri sa do DMs.").withMessageReference(messageId).block();
			dm.createMessage("Tvoj API token bol vytvoreny. Zatial na nom nemas ziadne prava, ale mozes si ich vyziadat od administratora.\n" +
					"Tvoj token je:" + hash).block();
		}
		else {
			channel.createMessage("Nastala chyba pri generovani tokenu. Kontaktujte prosim vyvojara.").block();
		}
	}

	@Override
	public String getName() {
		return "register";
	}

	@Override
	public String getDescription() {
		return "Pouzivatel, ktory napise tento prikaz, si vyziada pristup k API.";
	}

	@Override
	public String getUsage() {
		return "register";
	}

	@Override
	public List<String> getAliases() {
		return List.of("reg");
	}
}
