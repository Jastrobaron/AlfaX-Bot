package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.service.OpenAiService;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.FileManager;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class TextToSpeechCommand implements ICommand {

	private Config config;

	public TextToSpeechCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (this.config.getBoolean("openai-disabled") || this.config.getBoolean("openai-tts-disabled")) {
			chat.sendMessage("This command is disabled by the administrator!");
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		AtomicReference<String> messageContent = new AtomicReference<>(message.toString());
		if (messageContent.get().isEmpty()) {
			chat.getChannel().getMessageById(chat.getInvokerMessageId()).block().getReferencedMessage().ifPresent(msg -> messageContent.set(msg.getContent()));
		}

		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		CreateSpeechRequest speech = CreateSpeechRequest.builder()
				.model(this.config.getString("openai-tts-model"))
				.voice(this.config.getString("openai-tts-voice"))
				.input(messageContent.get())
				//.responseFormat("opus")
				.build();
		InputStream input = service.createSpeech(speech).byteStream();
		String filename = System.currentTimeMillis() + ".opus";
		File speechFile = FileManager.createTmpFile(filename);
		FileOutputStream outputStream = new FileOutputStream(speechFile);
		byte[] buffer = new byte[1024];
		int read;
		while ((read = input.read(buffer)) != -1) {
			outputStream.write(buffer, 0, read);
		}

		/*String ffmpegCmd = FormattedString.create("ffmpeg -f lavfi -i color=c=black:s=32x32:r=1 -i ${in_file} -c:v libvpx -c:a copy -f webm -")
				.addParam("in_file", speechFile.getAbsolutePath())
				.build();
		Process prc = new ProcessBuilder(ffmpegCmd.split(" "))
				.redirectErrorStream(true)
				.start();
		InputStream ffmpegOutput = prc.getInputStream();
		channel.createMessage(spec -> {
			try {
				spec.addFile("out.webm", ffmpegOutput);
				spec.setMessageReference(messageId);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).block();*/

		chat.getChannel().createMessage(spec -> {
			try {
				spec.addFile("speech.mp3", new FileInputStream(speechFile));
				spec.setMessageReference(chat.getInvokerMessageId());
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

	@Override
	public int getCooldown() {
		return 0;
	}
}
