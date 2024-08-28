package xyz.rtsvk.alfax.services.mqtt.actions;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.util.Logger;

public interface Action {
	void handle(String topic, MqttMessage message, GatewayDiscordClient client, Logger logger) throws Exception;
}
