package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class PollCreateCommand implements ICommand {
	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (chat.getChannel() instanceof PrivateChannel pc) {
			pc.createMessage("You can't create a poll in a private channel").block();
			return;
		}

		if (args.size() < 2) {
			chat.sendMessage("You need to provide a question and at least 2 options");
			return;
		}

		boolean success = Database.createPoll(chat.getChannel().getId(), args.get(0), args.subList(1, args.size())); // Save the poll to the database
		if (!success) {
			chat.sendMessage("An error occurred while creating the poll");
		} else {
			StringBuilder poll = new StringBuilder();
			poll.append("**").append(args.get(0)).append("**\n");
			for (int i = 1; i < args.size(); i++) {
				poll.append(i).append(". ").append(args.get(i)).append("\n");
			}

			EmojiData data = EmojiData.builder()
					.animated(false)
					.name("white_check_mark")
					.build();

			Message message = chat.sendMessage(poll.toString());
			message.addReaction(ReactionEmoji.of(data)).block();
		}
	}

	@Override
	public String getName() {
		return "poll";
	}

	@Override
	public String getDescription() {
		return "Create a poll";
	}

	@Override
	public String getUsage() {
		return "poll <question> <option1> <option2> ...";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
