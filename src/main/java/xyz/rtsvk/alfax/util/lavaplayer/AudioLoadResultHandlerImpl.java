package xyz.rtsvk.alfax.util.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

public class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

    private final IChatContext chat;
    private final GuildState guildState;
    private final MessageManager language;

    public AudioLoadResultHandlerImpl(IChatContext chat, GuildState guildState, MessageManager language) {
        this.chat = chat;
        this.guildState = guildState;
        this.language = language;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        this.guildState.getTrackScheduler().schedule(audioTrack);
        this.chat.sendMessage(this.language.getFormattedString("feature.music.track-loaded")
                .addParam("title", audioTrack.getInfo().title)
                .addParam("author", audioTrack.getInfo().author)
                .build());
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        TrackScheduler scheduler = this.guildState.getTrackScheduler();
        for (AudioTrack track : audioPlaylist.getTracks()) {
            scheduler.schedule(track);
        }
        this.chat.sendMessage(this.language.getFormattedString("feature.music.playlist-loaded")
                .addParam("name", audioPlaylist.getName()).build());
    }

    @Override
    public void noMatches() {
        this.chat.sendMessage(this.language.getMessage("feature.music.no-matches"));
    }

    @Override
    public void loadFailed(FriendlyException e) {
        this.chat.sendMessage(this.language.getFormattedString("feature.music.load-failed")
                .addParam("error", e.getMessage()).build());
        e.printStackTrace();
    }
}
