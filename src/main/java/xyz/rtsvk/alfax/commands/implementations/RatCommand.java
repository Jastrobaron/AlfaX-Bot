package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class RatCommand implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
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
}
