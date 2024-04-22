package xyz.rtsvk.alfax.webserver.actions;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MqttPublishAction implements Action {

	private final Mqtt mqtt;

	public MqttPublishAction(Mqtt mqtt) {
		this.mqtt = mqtt;
	}

	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String topic = request.getProperty("topic").toString();
		String msg = request.getProperty("message").toString();
		if (msg.isEmpty())
			return ActionResult.badRequest("Message is empty");

		try {
			mqtt.publish(topic, new MqttMessage(msg.getBytes(StandardCharsets.UTF_8)));
		}
		catch (MqttException e) {
			e.printStackTrace();
			return ActionResult.internalError("MQTT error: " + e.getMessage());
		}

		return ActionResult.ok("Message published");
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_MQTT;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of("topic", "message");
	}

	@Override
	public String getEndpointName() {
		return "/mqtt_publish";
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.POST);
	}
}
