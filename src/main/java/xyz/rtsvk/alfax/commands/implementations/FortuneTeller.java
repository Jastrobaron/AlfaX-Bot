package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;
import java.util.Properties;
import java.util.Random;

public class FortuneTeller implements Command {
	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		if (args.size() == 1) channel.createMessage("**Nic si sa nespytal!**").block();

		else {
			String[] answers = new String[] {
					"Urcite! :100:",
					"Nepochybujem o tom. :slight_smile:",
					"Nech sa poserem ak to nie je pravda. :poop:",
					"Netusim bracho. :thinking:",
					"Nemyslim si. :no_entry_sign:",
					"A to ti jak napadlo? :face_with_raised_eyebrow:",
					"Jebe ti? To si z kerej pici vytiahol? :flushed:",
					"Chod do pice :middle_finger:"
			};

			channel.createMessage("**" + answers[new Random().nextInt(answers.length)] + "**").block();
		}


	}

	@Override
	public String getName() {
		return "8ball";
	}

	@Override
	public String getDescription() {
		return "Spytaj sa otazku typu ano/nie a dostanes odpoved!";
	}

	@Override
	public String getUsage() {
		return "8ball <otazka>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("8b", "fortune");
	}
}
