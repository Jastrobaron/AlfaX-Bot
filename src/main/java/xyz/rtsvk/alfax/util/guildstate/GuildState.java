package xyz.rtsvk.alfax.util.guildstate;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.lavaplayer.LavaPlayerAudioProvider;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;

import java.util.function.Supplier;

/**
 * Class representing a guild state to use with commands.
 * Stores the current music queue and some more stuff
 * @author Jastrobaron
 */
public class GuildState {

    /** ID of the Discord server (guild) */
    private final Snowflake guildId;
    /** Audio player for the guild */
    private final AudioPlayer player;
    /** Audio track scheduler for the guild */
    private final TrackScheduler trackScheduler;
    /** Audio provider */
    private final AudioProvider audioProvider;
    /** Bot represented as a member, cached in order to not destroy Discord API */
    private Member botMember;
    /** Last chat of the guild the bot received a command from */
    private IChatContext lastCommandChat;
    /** Voice connection object */
    private VoiceConnection voiceConnection;
    /** Voice channel the bot is currently active in */
    private VoiceChannel voiceChannel;

    /**
     * Constructor
     * @param guildId ID of the guild
     */
    public GuildState(Snowflake guildId, AudioPlayer player) {
        this.guildId = guildId;
        this.player = player;
        this.audioProvider = new LavaPlayerAudioProvider(player);
        this.trackScheduler = new TrackScheduler(this);
        this.lastCommandChat = null;
        this.botMember = null;
        this.voiceConnection = null;
        this.voiceChannel = null;
    }

    /**
     * Join the voice channel. Leaves if in another voice channel.
     * @param voiceChannel to join
     */
    public synchronized void joinVoiceChannel(VoiceChannel voiceChannel) {
        if (isVoiceConnected() && !voiceChannel.equals(this.voiceChannel)) {
            leaveVoiceChannel();
        }
        this.voiceConnection = voiceChannel.join()
                .withProvider(this.audioProvider)
                .withSelfDeaf(true).block();
        this.voiceChannel = voiceChannel;
    }

    /**
     * Leave the voice channel.
     */
    public synchronized void leaveVoiceChannel() {
        this.voiceConnection.disconnect();
        this.voiceChannel.sendDisconnectVoiceState().block();
    }

    /**
     * @return ID of the guild
     */
    public synchronized Snowflake getGuildId() {
        return this.guildId;
    }

    /**
     * @return audio player of the guild
     */
    public synchronized AudioPlayer getPlayer() {
        return player;
    }

    /**
     * @return track scheduler of the guild
     */
    public synchronized TrackScheduler getTrackScheduler() {
        return this.trackScheduler;
    }

    public synchronized Member getMember() {
        return this.botMember;
    }

    public synchronized AudioProvider getAudioProvider() {
        return this.audioProvider;
    }

    public synchronized IChatContext getLastCommandChat() {
        return this.lastCommandChat;
    }

    public synchronized void setLastCommandChat(IChatContext lastCommandChat) {
        this.lastCommandChat = lastCommandChat;
    }

    public synchronized boolean isVoiceConnected() {
        return this.voiceConnection != null && this.voiceConnection.isConnected().blockOptional().orElse(false);
    }
}
