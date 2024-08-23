package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Instant;
import java.util.List;

public class CreditsCommand implements ICommand {

	private final Config config;

	public CreditsCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

		String osName = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String osArch = System.getProperty("os.arch");
		String javaVersion = System.getProperty("java.version");

		EmbedCreateSpec.Builder tableBuilder = EmbedCreateSpec.builder()
				.title("Alfa-X Bot")
				.url("https://github.com/Jastrobaron/AlfaX-Bot")
				.addField("Created and maintained by", "[Jastrobaron](https://github.com/Jastrobaron)", false)
				.addField("Running on", osName + ", " + osVersion + ", " + osArch, false)
				.addField("Java", javaVersion, false);

		if (!this.config.getBoolean("openai-disabled")) {
			tableBuilder
					.addField("OpenAI Chat Completion model", this.config.getString("openai-chat-model"), false)
					.addField("OpenAI Voice model", this.config.getString("openai-tts-model"), false)
					.addField("OpenAI Voice voice", this.config.getString("openai-tts-voice"), false);
		}
		else {
			tableBuilder.addField("OpenAI", "Disabled", false);
		}

		if (this.config.getBoolean("webserver-enabled")) {
			tableBuilder.addField("Web server", "Running at port " + this.config.getString("webserver-port"), false);
		}
		else {
			tableBuilder.addField("Web server", "Disabled", false);
		}

		chat.sendMessage(tableBuilder.timestamp(Instant.now()).build());
	}

	@Override
	public String getName() {
		return "credits";
	}

	@Override
	public String getDescription() {
		return "command.credits.description";
	}

	@Override
	public String getUsage() {
		return "credits";
	}

	@Override
	public List<String> getAliases() {
		return List.of("creds", "info");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
