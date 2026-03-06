package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Database;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

public class RegisterSensorCommand implements Command {

	private final String prefix;

	public RegisterSensorCommand(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		// String key, String type, String unit, float min, float max

		if (args.size() < 5) {
			channel.createMessage("Usage: " + this.prefix + "senreg <type> <unit> <min> <max>").block();
			return;
		}

		String key = generateKey(128);
		String type = args.get(1);
		String unit = args.get(2);
		float min = Float.parseFloat(args.get(3));
		float max = Float.parseFloat(args.get(4));

		boolean success = Database.registerSensor(key, "", type, unit, min, max);
		if (success) channel.createMessage("Sensor '" + key + "' registered.").block();
		else channel.createMessage("Failed to register sensor. Please contact developer.").block();
	}

	@Override
	public String getName() {
		return "register_sensor";
	}

	private String generateKey(int length) throws NoSuchAlgorithmException {

		StringBuilder input = new StringBuilder();
		for(int i = 0; i < 16; i++) {
			input.append((char) (Math.random() * 26 + 97));
		}

		String result = "";
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(input.toString().getBytes());
		BigInteger hash = new BigInteger(1, md.digest());
		result = hash.toString(16);
		while(result.length() < length) {
			result = "0" + result;
		}
		return result;
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
}
