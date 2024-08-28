package xyz.rtsvk.alfax.services.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.services.mqtt.actions.Action;
import xyz.rtsvk.alfax.services.mqtt.actions.UnknownTopicAction;
import xyz.rtsvk.alfax.util.Logger;

import java.util.Map;

public class MqttHandler implements MqttCallback {

	private final Logger logger;
	private final GatewayDiscordClient gateway;
	private final Map<String, Action> actionMap;

	public MqttHandler(GatewayDiscordClient gateway, Map<String, Action> actionMap) {
		this.actionMap = actionMap;
		this.logger = new Logger(this.getClass());
		this.gateway = gateway;
	}

	@Override
	public void connectionLost(Throwable cause) {
		this.logger.error("Connection lost! " + cause.getLocalizedMessage());
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		this.logger.info("Message arrived on topic [" + topic + "]: " + message.toString());
		Action action = this.actionMap.getOrDefault(topic, new UnknownTopicAction());
		action.handle(topic, message, this.gateway, this.logger);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}
