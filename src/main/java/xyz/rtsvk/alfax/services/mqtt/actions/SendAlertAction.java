package xyz.rtsvk.alfax.services.mqtt.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.alfax.util.Logger;

import java.nio.charset.StandardCharsets;

public class SendAlertAction implements Action {
	@Override
	public void handle(String topic, MqttMessage message, GatewayDiscordClient client, Logger logger) throws Exception {
		String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
		JSONObject json = (JSONObject) (new JSONParser().parse(msg));

		String alert = (String) json.get("alert");
		String channelId = (String) json.get("channel_id");

		if (alert == null || channelId == null) {
			logger.error("Invalid alert message: " + msg);
			return;
		}

		client.rest().getChannelById(Snowflake.of(channelId)).createMessage(alert).subscribe();
	}
}
