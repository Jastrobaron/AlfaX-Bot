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

public class UserPermissionsCommand implements ICommand {

	private final Config cfg;

	public UserPermissionsCommand(Config cfg) {
		this.cfg = cfg;
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_ADMIN)) {
			chat.sendMessage("Nemas opravnenie na vykonanie tohto prikazu.");
			return;
		}

		if (args.size() < 2) {
			chat.sendMessage("Pouzitie: " + this.cfg.getString("prefix") + "usermod <id> <opravnenia>");
			return;
		}

		String mention = args.get(0);
		if (!mention.startsWith("<@") || !mention.endsWith(">")) {
			chat.sendMessage("Nespravny format uzivatela. Pouzite @mention, prosim.");
			return;
		}
		String id = mention.substring(2, mention.length() - 1);
		int permissions = Integer.parseInt(args.get(1));
		boolean success = Database.updateUserPermissions(id, permissions);
		if (success) {
			chat.sendMessage("Uzivatel " + mention + " ma teraz opravnenia " + permissions);
		} else {
			chat.sendMessage("Nepodarilo sa upravit opravnenia uzivatela " + id);
		}
	}

	@Override
	public String getName() {
		return "usermodify";
	}

	@Override
	public String getDescription() {
		return "Edit user permissions. Admin only.";
	}

	@Override
	public String getUsage() {
		return "usermodify <id> <permissions>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("usermod");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
