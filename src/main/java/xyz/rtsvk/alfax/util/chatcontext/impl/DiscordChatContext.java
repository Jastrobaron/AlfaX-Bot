package xyz.rtsvk.alfax.util.chatcontext.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateMono;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DiscordChatContext implements IChatContext {

	private final ScheduledExecutorService scheduler;
	private final MessageChannel discordChannel;
	private final Message invokerMessage;
	private final String commandPrefix;
	private ScheduledFuture<?> task;

	public DiscordChatContext(MessageChannel discordChannel, Message invokerMessage, String commandPrefix) {
		this.discordChannel = discordChannel;
		this.invokerMessage = invokerMessage;
		this.commandPrefix = commandPrefix;
		this.scheduler = Executors.newScheduledThreadPool(1);
		this.task = null;
	}

	@Override
	public Message sendMessage(String message) {
		return sendMessage(message, this.invokerMessage.getId());
	}

	@Override
	public Message sendMessage(String message, Snowflake reference) {
		if (this.task != null) {
			this.task.cancel(true);
			this.task = null;
		}
		return this.message(reference).withContent(message).block();
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec) {
		return this.sendMessage(spec, this.invokerMessage.getId());
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec, Snowflake reference) {
		if (this.task != null) {
			this.task.cancel(true);
			this.task = null;
		}
		return this.message(reference).withEmbeds(spec).block();
	}

	@Override
	public void startTyping() {
		if (this.task != null) {
			return;
		}
		this.task = this.scheduler.scheduleAtFixedRate(this.discordChannel.type()::subscribe, 0, 5, TimeUnit.SECONDS);
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

	private MessageCreateMono message(Snowflake reference) {
		return this.discordChannel.createMessage().withMessageReference(reference);
	}
}
