package xyz.rtsvk.alfax.util.chatcontext.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Mono;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;

public class DiscordChatContext implements IChatContext {

	private final MessageChannel discordChannel;
	private final Message invokerMessage;
	private final Publisher<Void> msgSent;
	private final String commandPrefix;
	private Snowflake lastMessageId;
	private boolean isTyping;

	public DiscordChatContext(MessageChannel discordChannel, Message invokerMessage, String commandPrefix) {
		this.discordChannel = discordChannel;
		this.invokerMessage = invokerMessage;
		this.commandPrefix = commandPrefix;
		this.msgSent = Subscriber::onComplete;
		this.lastMessageId = null;
		this.isTyping = false;
	}

	@Override
	public Message sendMessage(String message) {
		return sendMessage(message, this.invokerMessage.getId());
	}

	@Override
	public Message sendMessage(String message, Snowflake reference) {
		Message msg = this.discordChannel.createMessage(message).withMessageReference(reference).block();
		if (this.isTyping) {
			this.msgSent.subscribe(null);
			this.isTyping = false;
		}
		if (msg != null) {
			this.lastMessageId = msg.getId();
		}
		return msg;
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec) {
		if (this.isTyping) {
			this.msgSent.subscribe(null);
			this.isTyping = false;
		}
		return this.discordChannel.createMessage(spec).withMessageReference(this.invokerMessage.getId()).block();
	}

	@Override
	public void startTyping() {
		this.isTyping = true;
		this.discordChannel.typeUntil(this.msgSent).subscribe();
	}

	@Override
	public MessageChannel getChannel() {
		return this.discordChannel;
	}

	@Override
	public Snowflake getLastMessageId() {
		return this.lastMessageId;
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
