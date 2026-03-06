package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;

import java.util.List;
import java.util.Properties;

public class RedeemAdminPermissionCommand implements Command {

	private final Config config;

	public RedeemAdminPermissionCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		int adminCount = Database.getAdminCount();
		if (adminCount == -1) {
			channel.createMessage("Nastala chyba pri vykonavani tohto prikazu. Prosim, kontaktujte vyvojara.").block();
			return;
		}

		if (adminCount > 0) {
			channel.createMessage("Tento prikaz moze vykonat iba prvy admin.").block();
			return;
		}

		if (!Database.userExists(user.getId().asString())) {
			channel.createMessage("Neexistujes :skull:").block();
			return;
		}

		String token = config.getString("admin-token");
		String userToken = args.get(1);
		if (token.equals(userToken)) {
			Database.updateUserPermissions(user.getId().asString(), Database.PERMISSION_ADMIN);
			channel.createMessage("Pouzivatel " + user.getMention() + " bol povyseny na admina.").block();
			config.remove("admin-token");
		}
		else {
			channel.createMessage("Zadali ste nespravny token.").block();
		}
	}

	@Override
	public String getName() {
		return "redeem";
	}

	@Override
	public String getDescription() {
		return "Redeem admin permission token.";
	}

	@Override
	public String getUsage() {
		return "redeem <token>";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}
}
