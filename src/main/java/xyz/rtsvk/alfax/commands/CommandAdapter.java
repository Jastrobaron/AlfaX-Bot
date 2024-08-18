package xyz.rtsvk.alfax.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class CommandAdapter implements ICommand{
    @Override
    public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
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
