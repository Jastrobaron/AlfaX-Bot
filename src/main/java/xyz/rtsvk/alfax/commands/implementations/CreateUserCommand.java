package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;

import java.util.List;

public class CreateUserCommand implements Command {
	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		String userId = user.getId().asString();
		String hash = Database.hash(userId);
		Database.addUser(userId, hash, 0);
		user.getPrivateChannel().block().createMessage("Tvoj API kluc je: " + hash).block();
	}

	@Override
	public String getDescription() {
		return "Pouzivatel, ktory napise tento prikaz, si vyziada pristup k API.";
	}
}
