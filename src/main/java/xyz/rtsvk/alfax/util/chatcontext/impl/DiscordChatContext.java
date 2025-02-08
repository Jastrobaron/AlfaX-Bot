package xyz.rtsvk.alfax.util.chatcontext.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;

public class DiscordChatContext implements IChatContext {

	private final MessageChannel discordChannel;
	private final Message invokerMessage;
	private final String commandPrefix;
	private MessageCreateMono msgCreator;
	private boolean creatorActive;

	public DiscordChatContext(MessageChannel discordChannel, Message invokerMessage, String commandPrefix) {
		this.discordChannel = discordChannel;
		this.invokerMessage = invokerMessage;
		this.commandPrefix = commandPrefix;
		this.msgCreator = null;
		this.creatorActive = false;
	}

	@Override
	public Message sendMessage(String message) {
		return sendMessage(message, this.invokerMessage.getId());
	}

	@Override
	public Message sendMessage(String message, Snowflake reference) {
		MessageCreateMono creator = this.creatorActive ? this.msgCreator : this.discordChannel.createMessage();
		this.creatorActive = false;
		return creator.withContent(message).withMessageReference(reference).block();
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec) {
		return this.sendMessage(spec, this.invokerMessage.getId());
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec, Snowflake reference) {
		MessageCreateMono creator = this.creatorActive ? this.msgCreator : this.discordChannel.createMessage();
		this.creatorActive = false;
		return creator.withEmbeds(spec).withMessageReference(reference).block();
	}

	@Override
	public void startTyping() {
		// NOTE: Creates a message with embeds, but none are supplied, might cause issues in the future
		this.msgCreator = this.discordChannel.createMessage();
		this.discordChannel.typeUntil(this.msgCreator).subscribe();
		this.creatorActive = true;
	}

	@Override
	public MessageChannel getChannel() {
		return this.discordChannel;
	}

	@Override
	public Snowflake getLastMessageId() {
		return this.discordChannel.getLastMessageId().orElse(null);
	}

	@Override
	public Message getInvokerMessage() {
		return this.invokerMessage;
	}

	@Override
	public String getCommandPrefix() {
		return this.commandPrefix;
	}

	@Override
	public boolean isPrivate() {
		return this.discordChannel instanceof PrivateChannel;
	}
}
