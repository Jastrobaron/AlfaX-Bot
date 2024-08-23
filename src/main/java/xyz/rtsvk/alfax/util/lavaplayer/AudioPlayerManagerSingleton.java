package xyz.rtsvk.alfax.util.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;

/**
 * Singleton instance of {@link AudioPlayerManager}, used by the entire application
 * @author Jastrobaron
 */
public class AudioPlayerManagerSingleton {

    /** Instance of {@link AudioPlayerManager} */
    private static AudioPlayerManager instance;

    /**
     * @return the instance of {@link AudioPlayerManager}
     */
    public static AudioPlayerManager get() {
        if (instance == null) {
            instance = new DefaultAudioPlayerManager();
            instance.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
            instance.registerSourceManager(new YoutubeAudioSourceManager(true,
                    new Music(), new Web(), new AndroidTestsuite(), new TvHtml5Embedded()));
            AudioSourceManagers.registerRemoteSources(instance, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
            AudioSourceManagers.registerLocalSource(instance);
        }
        return instance;
    }
}
