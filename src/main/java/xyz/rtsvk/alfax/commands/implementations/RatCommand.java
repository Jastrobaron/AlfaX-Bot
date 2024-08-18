package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RatCommand implements ICommand {
	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		URL url = new URL("https://api.imgur.com/3/gallery/tag_info/rat");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		conn.setRequestProperty("Client-ID", "client_id");
	}

	@Override
	public String getName() {
		return "rat";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUsage() {
		return null;
	}

	@Override
	public List<String> getAliases() {
		return List.of("patkan");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
