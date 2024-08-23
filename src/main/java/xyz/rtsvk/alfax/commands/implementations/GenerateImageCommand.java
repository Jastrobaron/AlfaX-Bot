package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Duration;
import java.util.List;

public class GenerateImageCommand implements ICommand {
	private Config config;

	public GenerateImageCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (this.config.getBoolean("openai-disabled") || this.config.getBoolean("openai-image-disabled")) {
			chat.sendMessage(language.getMessage("command.generate-image.disabled"));
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		String messageContent = message.toString();
		if (messageContent.isEmpty()) {
			chat.sendMessage(language.getMessage("command.generate-image.no-prompt"));
			return;
		}

		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		CreateImageRequest image = CreateImageRequest.builder()
				.model(this.config.getString("openai-image-model"))
				.prompt(messageContent)
				.size(this.config.getString("openai-image-size"))
				.n(1).build();

		Image generatedImage = service.createImage(image).getData().get(0);
		chat.sendMessage(generatedImage.getUrl());
	}

	@Override
	public String getName() {
		return "image";
	}

	@Override
	public String getDescription() {
		return "command.generate-image.description";
	}

	@Override
	public String getUsage() {
		return "image <prompt>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("img");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
