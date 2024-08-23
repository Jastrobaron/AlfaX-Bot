package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class PauseCommand implements ICommand {
    @Override
    public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
        if (chat.isPrivate()) {
            chat.sendMessage(language.getMessage("general.command.dm-not-supported"));
            return;
        }

        guildState.getPlayer().setPaused(true);
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "command.pause.description";
    }

    @Override
    public String getUsage() {
        return "pause";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public int getCooldown() {
        return 0;
    }
}
