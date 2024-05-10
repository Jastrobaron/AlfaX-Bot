package xyz.rtsvk.alfax.reactions.impl;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import xyz.rtsvk.alfax.reactions.IReactionCallback;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

public class BookmarkReactionCallback implements IReactionCallback {
	@Override
	public void handle(Message message, User whoClicked, MessageManager lang, int reactionsCount) throws Exception {
		String guildId;
		if (message.getGuildId().isPresent()) {
			guildId = message.getGuildId().get().asString();
		} else {
			guildId = "@me";
		}
		String channelId = message.getChannelId().asString();
		String messageId = message.getId().asString();
		String messageURL = TextUtils.format(
				"https://discord.com/channels/${0}/${1}/${2}", guildId, channelId, messageId);
		PrivateChannel dm = whoClicked.getPrivateChannel().block();
		if (dm != null) {
			dm.createMessage(lang.getFormattedString("reaction.bookmark.message")
					.addParam("link", messageURL).build()).block();
		}
	}

	@Override
	public ReactionEmoji getEmoji() {
		return ReactionEmoji.unicode("\uD83D\uDD16");
	}
}
