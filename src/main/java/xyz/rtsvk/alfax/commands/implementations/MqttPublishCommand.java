package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MqttPublishCommand implements ICommand {

	private Mqtt client;
	private final Logger logger;
	private final String prefix;

	public MqttPublishCommand(Config cfg) {
		this.logger = new Logger(this.getClass());
		this.prefix = cfg.getString("prefix");

		if (!cfg.containsKey("mqtt-enabled")) {
			logger.error("MQTT is not enabled in config, skipping MQTT initialization");
			return;
		}

		this.client = new Mqtt(cfg, null);
		this.client.setClientId("MQTT-Publish-Command");
		this.client.setDoSubscribe(false);
		this.client.start();
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (!Database.checkPermissions(user.getId().asString(), Database.PERMISSION_MQTT)) {
			chat.sendMessage("Nemas opravnenie na pouzitie tohto prikazu.");
			return;
		}

		if (args.size() < 2) {
			chat.sendMessage(language.getFormattedString("command.mqtt-publish.usage")
					.addParam("prefix", this.prefix)
					.addParam("usage", this.getUsage())
					.build());
			return;
		}

		String topic = args.get(0);
		String message = String.join(" ", args.subList(1, args.size()));
		MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
		this.client.publish(topic, msg);
		this.logger.info("Published message to topic '" + topic + "': " + message);
	}

	public Mqtt getClient() {
		return client;
	}

	@Override
	public String getName() {
		return "mqtt-publish";
	}

	@Override
	public String getDescription() {
		return "command.mqtt-publish.description";
	}

	@Override
	public String getUsage() {
		return "mqtt <topic> <message>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("mqtt-pub", "pub");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
