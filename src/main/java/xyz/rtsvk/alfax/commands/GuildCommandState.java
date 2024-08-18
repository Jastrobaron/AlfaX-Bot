package xyz.rtsvk.alfax.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;

/**
 * Class representing a guild state to use with commands.
 * Stores the current music queue
 * @author Jastrobaron
 */
public class GuildCommandState {

    /** ID of the Discord server (guild) */
    private final Snowflake guildId;
    /** Audio player for the guild */
    private final AudioPlayer player;

    /**
     * Constructor
     * @param guildId ID of the guild
     */
    public GuildCommandState(Snowflake guildId, AudioPlayer player) {
        this.guildId = guildId;
        this.player = player;
    }

    /**
     * @return ID of the guild
     */
    public Snowflake getGuildId() {
        return this.guildId;
    }

    public AudioPlayer getPlayer() {
        return player;
    }
}
