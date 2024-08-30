package xyz.rtsvk.alfax.commands.implementations;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.lavaplayer.LavaPlayerUtils;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class MusicQueueCommand implements ICommand {
    @Override
    public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
        if (chat.isPrivate()) {
            chat.sendMessage(language.getMessage("general.command.dm-not-supported"));
            return;
        }

        Queue<AudioTrack> trackQueue = guildState.getTrackScheduler().getTrackQueue();
        int count = ((guildState.getPlayer().getPlayingTrack() != null) ? 1 : 0) + trackQueue.size();
        List<String> message = new ArrayList<>();
        message.add(language.getFormattedString("command.queue.track-count")
                .addParam("count", count).build());
        AudioTrack currentTrack = guildState.getPlayer().getPlayingTrack();
        if (currentTrack != null) {
            message.add("**" + LavaPlayerUtils.getTrackDisplayName(currentTrack) + "**");
        }
        for (AudioTrack track : trackQueue) {
            message.add(LavaPlayerUtils.getTrackDisplayName(track));
        }
        chat.sendMessage(String.join("\n", message));
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return "command.queue.description";
    }

    @Override
    public String getUsage() {
        return "queue";
    }

    @Override
    public List<String> getAliases() {
        return List.of("q");
    }

    @Override
    public int getCooldown() {
        return 0;
    }
}
