package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChatGPTCommand implements Command {

	private final Config config;

	public ChatGPTCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		if (this.config.getBoolean("openai-disabled")) {
			channel.createMessage("Tento prikaz je zakazany administratorom!").block();
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		String messageContent = message.toString();
		if (messageContent.isEmpty()) {
			channel.createMessage("Nic si sa nespytal bratm :skull:").block();
			return;
		}

		List<ChatMessage> history = new LinkedList<>();
		history.add(new ChatMessage(ChatMessageRole.USER.value(), messageContent));
		Message referencedMessage = getReferencedMessage(channel, messageId);
		while (referencedMessage != null) {
			if (referencedMessage.getAuthor().isEmpty()) return;
			String role = referencedMessage.getAuthor().get().getId().equals(bot.getSelfId())
					? ChatMessageRole.ASSISTANT.value() : ChatMessageRole.USER.value();
			history.add(new ChatMessage(role, referencedMessage.getContent()));
			referencedMessage = getReferencedMessage(channel, referencedMessage.getId());
		}
		history.sort(Collections.reverseOrder());  // reverse the list

		StringBuilder output = new StringBuilder();
		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
				.messages(history)
				.model(this.config.getString("openai-model"))
				.build();
		List<ChatCompletionChoice> choices = service.createChatCompletion(completionRequest).getChoices();
		choices.forEach(e -> {
			String text = e.getMessage().getContent();
			output.append(text);
		});

		String response = output.toString()
				.replace("@", "@\u200D");   // Prevent mentions
		String[] chunks = splitToChunks(response, 2048);
		Snowflake refMessageId = messageId;
		for (String chunk : chunks) {
			Message msg = channel.createMessage(chunk).withMessageReference(refMessageId).block();
			if (msg != null) refMessageId = msg.getId();
		}
	}

	private String[] splitToChunks(String response, int chunkSize) {
		int length = response.length();
		int chunkCount = (length + chunkSize - 1) / chunkSize;
		String[] chunks = new String[chunkCount];
		for (int i = 0; i < chunkCount; i++) {
			int start = i * chunkSize;
			int end = Math.min(length, start + chunkSize);
			chunks[i] = response.substring(start, end);
		}
		return chunks;
	}

	@Override
	public String getName() {
		return "chatgpt";
	}

	@Override
	public String getDescription() {
		return "Spytaj sa ChatGPT!";
	}

	@Override
	public String getUsage() {
		return "chatgpt <otazka>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("gpt");
	}

	private Message getReferencedMessage(MessageChannel channel, Snowflake messageId) {
		Message myMsg = channel.getMessageById(messageId).block();
		if (myMsg == null) return null;
		return myMsg.getReferencedMessage().orElse(null);
	}
}
