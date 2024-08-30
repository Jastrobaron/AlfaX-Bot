package xyz.rtsvk.alfax.util.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class TrackScheduler extends AudioEventAdapter {
	private final Queue<AudioTrack> queue;
	private final GuildState guildState;

	public TrackScheduler(GuildState guildState) {
		this.queue = new LinkedList<>();
		this.guildState = guildState;
		this.guildState.getPlayer().addListener(this);
	}

	@Override
	public void onPlayerPause(AudioPlayer player) {
		IChatContext chat = this.guildState.getLastCommandChat();
		Optional<User> userOpt = chat.getInvokerMessage().getAuthor();
		MessageManager language = userOpt.isPresent()
				? Database.getUserLanguage(userOpt.get().getId())
				: MessageManager.getDefaultLanguage();
        chat.sendMessage(language.getMessage("feature.music.song-paused"));
    }

	@Override
	public void onPlayerResume(AudioPlayer player) {
		IChatContext chat = this.guildState.getLastCommandChat();
		Optional<User> userOpt = chat.getInvokerMessage().getAuthor();
		MessageManager language = userOpt.isPresent()
				? Database.getUserLanguage(userOpt.get().getId())
				: MessageManager.getDefaultLanguage();
		chat.sendMessage(language.getMessage("feature.music.song-resumed"));
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		IChatContext chat = this.guildState.getLastCommandChat();
		Optional<User> userOpt = chat.getInvokerMessage().getAuthor();
		MessageManager language = userOpt.isPresent()
				? Database.getUserLanguage(userOpt.get().getId())
				: MessageManager.getDefaultLanguage();
		chat.sendMessage(language.getFormattedString("feature.music.song-start")
				.addParam("title", track.getInfo().title)
				.addParam("author", track.getInfo().author)
				.build());
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext && !this.queue.isEmpty()) {
			this.playNext(false);
		}

		// endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
		// endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
		// endReason == STOPPED: The player was stopped.
		// endReason == REPLACED: Another track started playing while this had not finished
		// endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
		//                       clone of this back to your queue
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		// An already playing track threw an exception (track end event will still be received separately)
	}

	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		// Audio track has been unable to provide us any audio, might want to just start a new track
	}

	public void schedule(AudioTrack audioTrack) {
		AudioPlayer player = this.getAudioPlayer();
		if (player.getPlayingTrack() != null) {
			this.queue.offer(audioTrack);
		} else {
			player.playTrack(audioTrack);
		}
	}

	public boolean skipCurrentTrack() {
		AudioTrack currentTrack = this.getAudioPlayer().getPlayingTrack();
		if (currentTrack == null) {
			return false;
		} else {
			this.playNext(true);
			return true;
		}
	}

	private void playNext(boolean interrupt) {
		this.getAudioPlayer().startTrack(this.queue.poll(), !interrupt);
	}

	public AudioPlayer getAudioPlayer() {
		return this.guildState.getPlayer();
	}

	public Queue<AudioTrack> getTrackQueue() {
		return this.queue;
	}
}
