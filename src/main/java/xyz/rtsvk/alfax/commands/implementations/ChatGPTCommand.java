package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatGPTCommand implements Command {

	private final Logger logger;

	public ChatGPTCommand() {
		this.logger = new Logger(this.getClass());
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		// Message channelMessage = channel.createMessage("Cakam na odpoved...").block();
		// MessageEditSpec spec = MessageEditSpec.builder().build();

		StringBuilder message = new StringBuilder();
		for (int i = 1; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		Process proc = Runtime.getRuntime().exec( "python3 askgpt.py " + message);
		int exitCode = proc.waitFor();

		if (exitCode != 0)
			channel.createMessage("Chyba: Proces skoncil s kodom " + proc.exitValue()).block();
		else {
			// read the input
			String output = "";
			try (InputStream in = proc.getInputStream()) {
				output = IOUtils.toString(in, StandardCharsets.UTF_8);
			}
			this.logger.info(output);
			JSONObject object = (JSONObject) (new JSONParser().parse(output));
			channel.createMessage(object.get("content").toString()).block();
		}

		// TODO: Use a non-deprecated way to edit the message.
		//channelMessage.edit(spec).block();
	}

	@Override
	public String getDescription() {
		return "Spytaj sa ChatGPT! (Pozor, chvilu trva, kym sa tento prikaz spracuje!)";
	}
}
