package xyz.rtsvk.alfax.util.chatcontext.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;

import java.util.ArrayList;
import java.util.List;

public class TestChatContext implements IChatContext {

	private final List<String> messages;
	private final boolean isPrivate;

	public TestChatContext(boolean isPrivate) {
		this.messages = new ArrayList<>();
		this.isPrivate = isPrivate;
	}

	@Override
	public Message sendMessage(String message) {
		return sendMessage(message, null);
	}

	@Override
	public Message sendMessage(String message, Snowflake reference) {
		this.messages.add(message);
		return null;
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec) {
		this.messages.add(spec.toString());
		return null;
	}

	@Override
	public Snowflake getLastMessageId() {
		return Snowflake.of(this.messages.size() - 1);
	}

	@Override
	public Message getInvokerMessage() {
		return null;
	}

	@Override
	public MessageChannel getChannel() {
		return null;
	}

	@Override
	public String getCommandPrefix() {
		return null;
	}

	@Override
	public boolean isPrivate() {
		return this.isPrivate;
	}
}
