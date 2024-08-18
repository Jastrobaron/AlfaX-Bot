package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.GuildCommandState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class CreditBuyCommand implements ICommand {

	@Override
	public void handle(User user, Chat chat, List<String> args, GuildCommandState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		long amount = !args.isEmpty() ? Long.parseLong(args.get(0)) : 100;
		if (Database.addUserCredits(user.getId(), amount)) {
			chat.sendMessage(language.getFormattedString("command.credit-buy.success").addParam("amount", amount).build());
		} else {
			chat.sendMessage(language.getMessage("command.credit-buy.error"));
		}
	}

	@Override
	public String getName() {
		return "credit_buy";
	}

	@Override
	public String getDescription() {
		return "command.credit-buy.description";
	}

	@Override
	public String getUsage() {
		return "credit_buy [amount]";
	}

	@Override
	public List<String> getAliases() {
		return List.of("cb", "buy");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
