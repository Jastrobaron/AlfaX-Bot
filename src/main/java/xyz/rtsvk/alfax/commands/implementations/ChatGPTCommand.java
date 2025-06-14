package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class ChatGPTCommand implements ICommand {

	private final Config config;

	public ChatGPTCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (this.config.getBoolean("openai-disabled")) {
			chat.sendMessage(language.getMessage("command.chatgpt.disabled"));
			return;
		}

		long credits = Database.getUserCredits(user.getId());
		if (credits == -1 ) {
			chat.sendMessage(language.getFormattedString("command.user.not-found")
					.addParam("prefix", this.config.getString("prefix")).build());
			return;
		}
		else if (credits < 1) {
			chat.sendMessage(language.getMessage("command.user.no-credits"));
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		String messageContent = message.toString();
		if (messageContent.isEmpty()) {
			chat.sendMessage(language.getMessage("command.chatgpt.no-prompt"));
			return;
		}

		List<ChatMessage> history = new LinkedList<>();
		history.add(new ChatMessage(ChatMessageRole.USER.value(), messageContent));
		Message referencedMessage = getReferencedMessage(chat.getChannel(), chat.getInvokerMessage().getId());
		StringBuilder botMessage = null;
		while (referencedMessage != null) {
			if (referencedMessage.getAuthor().isEmpty()) break;  // author is null (deleted message or message generated by webhook)
			boolean isBot = referencedMessage.getAuthor().get().getId().equals(bot.getSelfId());
			if (isBot) {
				if (botMessage == null) botMessage = new StringBuilder();
				botMessage.append(referencedMessage.getContent());
			} else if (botMessage != null) {
				history.add(new ChatMessage(ChatMessageRole.ASSISTANT.value(), botMessage.toString()));
				history.add(new ChatMessage(ChatMessageRole.USER.value(), referencedMessage.getContent()));
				botMessage = null;
			} else {
				history.add(new ChatMessage(ChatMessageRole.USER.value(), referencedMessage.getContent()));
			}
			referencedMessage = getReferencedMessage(chat.getChannel(), referencedMessage.getId());
		}
		history.sort((a,b) -> -1);  // reverse the list

		chat.startTyping();
		StringBuilder output = new StringBuilder();
		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
				.messages(history)
				.model(this.config.getString("openai-chat-model"))
				.build();
		ChatCompletionResult result = service.createChatCompletion(completionRequest);
		long tokenAmt = result.getUsage().getTotalTokens();
		Database.subtractUserCredits(user.getId(), tokenAmt);
		if (guildState != null) {
			Database.addTokenUsage(guildState.getGuildId(), tokenAmt);
		}
		List<ChatCompletionChoice> choices = result.getChoices();
		choices.forEach(e -> {
			String text = e.getMessage().getContent();
			output.append(text);
		});

		String response = output.toString()
				.replace("@", "@\u200D");   // Prevent mentions
		String[] chunks = splitToChunks(response, 2000);
		Snowflake refMessageId = chat.getInvokerMessage().getId();
		for (String chunk : chunks) {
			Message msg = chat.sendMessage(chunk, refMessageId);
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
		return "command.chatgpt.description";
	}

	@Override
	public String getUsage() {
		return "chatgpt <question>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("gpt");
	}

	@Override
	public int getCooldown() {
		return 0;
	}

	private Message getReferencedMessage(MessageChannel channel, Snowflake messageId) {
		Message myMsg = channel.getMessageById(messageId).block();
		if (myMsg == null) return null;
		return myMsg.getReferencedMessage().orElse(null);
	}
}
