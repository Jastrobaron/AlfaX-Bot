package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class LeaveCommand implements ICommand {
    @Override
    public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
        if (chat.isPrivate()) {
            chat.sendMessage(language.getMessage("general.command.dm-not-supported"));
            return;
        }

        if (guildState.isVoiceConnected()) {
            guildState.leaveVoiceChannel();
        } else {
            chat.sendMessage(language.getMessage("command.leave.not-in-voice"));
        }
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "command.leave.description";
    }

    @Override
    public String getUsage() {
        return "leave";
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
