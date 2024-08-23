package xyz.rtsvk.alfax.util.guildstate;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.util.lavaplayer.AudioPlayerManagerSingleton;

import java.util.HashMap;
import java.util.Map;

public class GuildStateRegister {

    private static final Map<Snowflake, GuildState> guildStates;

    static {
        guildStates = new HashMap<>();
    }

    public synchronized static GuildState getGuildState(Snowflake guildId) {
        if (guildId == null) {
            return null;
        }
        return guildStates.computeIfAbsent(guildId, GuildStateRegister::createGuildState);
    }

    private synchronized static GuildState createGuildState(Snowflake id) {
        AudioPlayer player = AudioPlayerManagerSingleton.get().createPlayer();
        return new GuildState(id, player);
    }
}
