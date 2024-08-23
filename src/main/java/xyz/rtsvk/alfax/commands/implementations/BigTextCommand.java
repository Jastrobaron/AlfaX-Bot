package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.HashMap;
import java.util.List;

public class BigTextCommand implements ICommand {

	private final HashMap<Character, String> emojis;
	private final HashMap<Character, Character> normalizer;

	public BigTextCommand() {
		this.emojis = new HashMap<>();
		this.normalizer = new HashMap<>();

		// digits
		this.emojis.put('0', ":zero:");
		this.emojis.put('1', ":one:");
		this.emojis.put('2', ":two:");
		this.emojis.put('3', ":three:");
		this.emojis.put('4', ":four:");
		this.emojis.put('5', ":five:");
		this.emojis.put('6', ":six:");
		this.emojis.put('7', ":seven:");
		this.emojis.put('8', ":eight:");
		this.emojis.put('9', ":nine:");

		// misc characters:
		this.emojis.put('!', ":exclamation:");
		this.emojis.put('?', ":question:");
		this.emojis.put('$', ":moneybag:");
		this.emojis.put('#', ":hash:");
		this.emojis.put(' ', "   ");

		// normalizer
		this.normalizer.put('č', 'c');
		this.normalizer.put('ć', 'c');
		this.normalizer.put('š', 's');
		this.normalizer.put('ś', 'c');
		this.normalizer.put('ě', 'e');
		this.normalizer.put('é', 'e');
		this.normalizer.put('ř', 'r');
		this.normalizer.put('ŕ', 'r');
		this.normalizer.put('ľ', 'l');
		this.normalizer.put('ĺ', 'l');
		this.normalizer.put('ť', 't');
		this.normalizer.put('ž', 'z');
		this.normalizer.put('á', 'a');
		this.normalizer.put('ä', 'a');
		this.normalizer.put('ó', 'o');
		this.normalizer.put('ô', 'o');
		this.normalizer.put('í', 'i');
		this.normalizer.put('ý', 'y');
		this.normalizer.put('ú', 'u');
		this.normalizer.put('ď', 'd');
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String message = String.join(" ", args);

		// normalize characters
		StringBuilder messageBuilder = new StringBuilder(message.toLowerCase());
		for (int i = 0; i < messageBuilder.length(); i++) {
			if (this.normalizer.containsKey(messageBuilder.charAt(i)))
				messageBuilder.setCharAt(i, this.normalizer.get(messageBuilder.charAt(i)));
		}

		String msg = messageBuilder.toString();
		StringBuilder outputBuilder = new StringBuilder();
		for (int i = 0; i < msg.length(); i++) {
			if (emojis.containsKey(msg.charAt(i))) {
				outputBuilder.append(this.emojis.get(msg.charAt(i))).append(" ");
			}
			else if (msg.charAt(i) >= 'a' && msg.charAt(i) <= 'z') {
				outputBuilder.append(":regional_indicator_").append(msg.charAt(i)).append(": ");
			}
			else {
				outputBuilder.append(msg.charAt(i));
			}
		}

		chat.sendMessage(outputBuilder.toString());
	}

	@Override
	public String getName() {
		return "bigtext";
	}

	@Override
	public String getDescription() {
		return "command.bigtext.description";
	}

	@Override
	public String getUsage() {
		return "bigtext <message>";
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
