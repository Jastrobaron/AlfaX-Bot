package xyz.rtsvk.alfax.util.guildstate;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.util.lavaplayer.AudioPlayerManagerSingleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to store current guild states
 * @author Jastrobaron
 */
public class GuildStateRegister {

    /** Map holding the guild states */
    private static final Map<Snowflake, GuildState> guildStates;

    static {
        guildStates = new HashMap<>();
    }

    /**
     * Returns the guild state corresponding to the supplied guild ID
     * @param guildId of the guild
     * @return state of the guild
     */
    public synchronized static GuildState getGuildState(Snowflake guildId) {
        if (guildId == null) {
            return null;
        }
        return guildStates.computeIfAbsent(guildId, GuildStateRegister::createGuildState);
    }

    /**
     * Helper method to create a new state object for the guild
     * @param id of the guild
     * @return the newly created guild state object
     */
    private synchronized static GuildState createGuildState(Snowflake id) {
        AudioPlayer player = AudioPlayerManagerSingleton.get().createPlayer();
        return new GuildState(id, player);
    }
}
