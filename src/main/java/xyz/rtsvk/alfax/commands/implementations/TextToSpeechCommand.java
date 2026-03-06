package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.FileManager;

import java.io.*;
import java.time.Duration;
import java.util.List;

public class TextToSpeechCommand implements Command {

	private final Config config;

	public TextToSpeechCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		if (this.config.getBoolean("openai-disabled")) {
			channel.createMessage("This command is disabled by the administrator!").block();
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		String messageContent = message.toString();
		if (messageContent.isEmpty()) {
			channel.createMessage("No text supplied!").block();
			return;
		}

		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		CreateSpeechRequest speech = CreateSpeechRequest.builder()
				.model(this.config.getString("openai-tts-model"))
				.voice(this.config.getString("openai-tts-voice"))
				.input(messageContent)
				.build();
		InputStream input = service.createSpeech(speech).byteStream();
		File speechFile = FileManager.createTmpFile(".mp3");
		FileOutputStream outputStream = new FileOutputStream(speechFile);
		byte[] buffer = new byte[1024];
		int read;
		while ((read = input.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}

		channel.createMessage(spec -> {
			try {
				spec.addFile("speech.mp3", new FileInputStream(speechFile));
				spec.setMessageReference(messageId);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}).block();
		speechFile.delete();
	}

	@Override
	public String getName() {
		return "speak";
	}

	@Override
	public String getDescription() {
		return "Uses OpenAI's Text-to-Speech model to generate speech from text.";
	}

	@Override
	public String getUsage() {
		return "speak <text>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("tts");
	}
}
