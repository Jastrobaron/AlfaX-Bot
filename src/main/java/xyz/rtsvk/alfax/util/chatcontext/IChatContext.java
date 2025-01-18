package xyz.rtsvk.alfax.util.chatcontext;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

public interface IChatContext {
	Message sendMessage(String message);
	Message sendMessage(String message, Snowflake reference);
	Message sendMessage(EmbedCreateSpec spec);
	void startTyping();
	Snowflake getLastMessageId();
	Message getInvokerMessage();
	MessageChannel getChannel();
	String getCommandPrefix();
	boolean isPrivate();

}
