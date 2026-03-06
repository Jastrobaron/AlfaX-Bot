package xyz.rtsvk.alfax.commands.sets;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;

import java.util.List;
import java.util.Properties;

public class MusicPlayerCommandSet extends CommandSet {
	public MusicPlayerCommandSet() {
		super("music_player");
		addCommand("play", () -> {

		});
	}
}
