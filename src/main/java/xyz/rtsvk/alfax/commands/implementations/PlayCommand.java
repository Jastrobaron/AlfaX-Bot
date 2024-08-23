package xyz.rtsvk.alfax.commands.implementations;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.lavaplayer.AudioLoadResultHandlerImpl;
import xyz.rtsvk.alfax.util.lavaplayer.AudioPlayerManagerSingleton;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class PlayCommand extends JoinVoiceCommand {

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (!guildState.isVoiceConnected()) {
			super.handle(user, chat, args, guildState, bot, language);
		}

		AudioPlayer player = guildState.getPlayer();
		AudioTrack track = player.getPlayingTrack();
		if (track != null && player.isPaused()) {
			player.setPaused(false);
			return;
		}

		String url = String.join(" ", args);
		AudioPlayerManagerSingleton.get()
				.loadItem(url, new AudioLoadResultHandlerImpl(chat, guildState, language));
	}

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public String getDescription() {
		return "Joins the voice channel you are in and plays the specified song.";
	}

	@Override
	public String getUsage() {
		return "play <link>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("p");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
