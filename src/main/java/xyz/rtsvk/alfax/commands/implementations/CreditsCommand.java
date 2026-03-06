package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.commands.Command;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

public class CreditsCommand implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		String javaVersion = System.getProperty("java.version");

		EmbedCreateSpec table = EmbedCreateSpec.builder()
				.title("Alfa-X Bot")
				.url("https://github.com/Jastrobaron/AlfaX-Bot")
				.addField("Created and maintained by", "[Jastrobaron](https://github.com/Jastrobaron)", false)
				.addField("Running on", osName + ", " + osVersion + ", " + osArch, false)
				.addField("Java", javaVersion, false)
				.timestamp(Instant.now())
				.build();

		channel.createMessage(table).block();
	}

	@Override
	public String getName() {
		return "credits";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getUsage() {
		return "credits";
	}

	@Override
	public List<String> getAliases() {
		return List.of("creds");
	}
}
