package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.alfax.commands.Command;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;


public class CatCommand implements Command {

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		try {
			URL url = new URL("https://api.thecatapi.com/v1/images/search");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			BufferedInputStream input = new BufferedInputStream(conn.getInputStream());

			StringBuilder response = new StringBuilder();
			byte[] buffer = new byte[2048];
			while (input.available() > 0) {
				int read = input.read(buffer);
				response.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
			}
			JSONArray json = (JSONArray) (new JSONParser().parse(response.toString()));
			JSONObject image = (JSONObject) json.get(0);

			input.close();
			conn.disconnect();

			EmbedCreateSpec table = EmbedCreateSpec.builder()
				.author("thecatapi.com", "https://thecatapi.com/", "")
				.title("Kočička (mačička)")
	    		.image(image.get("url").toString())
				.timestamp(Instant.now())
				.build();

			channel.createMessage(table).block();
		} catch (Exception e) {
			channel.createMessage("Kočky došly :(").block();
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "cat";
	}

	@Override
	public String getDescription() {
		return "Zobrazí krásnou kočičku (mačičku)";
	}

	@Override
	public String getUsage() {
		return "cat";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}
}
