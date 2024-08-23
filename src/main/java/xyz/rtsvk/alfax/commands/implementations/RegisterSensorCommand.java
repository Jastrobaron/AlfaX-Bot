package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.List;

public class RegisterSensorCommand implements ICommand {

	private final String prefix;

	public RegisterSensorCommand(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) throws Exception {
		// String key, String type, String unit, float min, float max

		if (args.size() < 5) {
			chat.sendMessage("Usage: " + this.prefix + "senreg <type> <unit> <min> <max>");
			return;
		}

		String key = TextUtils.getRandomString(128);
		String type = args.get(1);
		String unit = args.get(2);
		float min = Float.parseFloat(args.get(3));
		float max = Float.parseFloat(args.get(4));

		boolean success = Database.registerSensor(key, "", type, unit, min, max);
		if (success) chat.sendMessage("Sensor '" + key + "' registered.");
		else chat.sendMessage("Failed to register sensor. Please contact developer.");
	}

	@Override
	public String getName() {
		return "register_sensor";
	}

	@Override
	public String getDescription() {
		return "Register sensor command";
	}

	@Override
	public String getUsage() {
		return "senreg <type> <unit> <min> <max>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("senreg");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
