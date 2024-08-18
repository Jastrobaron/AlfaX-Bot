package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class RedeemAdminPermissionCommand implements ICommand {

	private final Config config;

	public RedeemAdminPermissionCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

		int adminCount = Database.getAdminCount();
		if (adminCount == -1) {
			chat.sendMessage("Nastala chyba pri vykonavani tohto prikazu. Prosim, kontaktujte vyvojara.");
			return;
		}

		if (adminCount > 0) {
			chat.sendMessage("Tento prikaz moze vykonat iba prvy admin.");
			return;
		}

		if (!Database.userExists(user.getId().asString())) {
			chat.sendMessage("Neexistujes :skull:");
			return;
		}

		String token = config.getString("admin-token");
		String userToken = args.get(1);
		if (token.equals(userToken)) {
			Database.updateUserPermissions(user.getId().asString(), Database.PERMISSION_ADMIN);
			chat.sendMessage("Pouzivatel " + user.getMention() + " bol povyseny na admina.");
			config.remove("admin-token");
		}
		else {
			chat.sendMessage("Zadali ste nespravny token.");
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

	@Override
	public int getCooldown() {
		return 0;
	}
}
