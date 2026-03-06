package xyz.rtsvk.alfax.commands.implementations;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.voice.AudioProvider;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;

import java.util.List;
import java.util.Properties;

public class PlayCommand implements Command {


	private final AudioPlayerManager playerManager;
	private final AudioPlayer player;
	private final AudioProvider provider;
	private final TrackScheduler scheduler;

	public PlayCommand(AudioPlayerManager playerManager, AudioPlayer player, AudioProvider provider, TrackScheduler scheduler) {
		this.playerManager = playerManager;
		this.player = player;
		this.provider = provider;
		this.scheduler = scheduler;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		if (args.isEmpty()) {
			channel.createMessage("Please provide a link to a song.").subscribe();
			return;
		}

		String link = args.get(0);
		if (!link.startsWith("http")) {
			link = "ytsearch:" + link;
		}

		playerManager.loadItem(link, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack audioTrack) {
				channel.createMessage("Adding to queue: " + audioTrack.getInfo().title).subscribe();
				scheduler.queue(audioTrack);
			}

			@Override
			public void playlistLoaded(AudioPlaylist audioPlaylist) {

			}

			@Override
			public void noMatches() {

			}

			@Override
			public void loadFailed(FriendlyException e) {

			}
		});
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
}
