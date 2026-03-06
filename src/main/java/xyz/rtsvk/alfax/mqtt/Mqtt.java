package xyz.rtsvk.alfax.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import xyz.rtsvk.alfax.mqtt.actions.Action;
import xyz.rtsvk.alfax.mqtt.actions.SendAlertAction;
import xyz.rtsvk.alfax.mqtt.actions.SensorDataReceivedAction;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class Mqtt extends Thread {

	private final GatewayDiscordClient gateway;
	private MqttClient client;
	private final Logger logger;

	private final String uri;
	private String clientid;
	private final String uname;
	private final String pwd;
	private boolean doSubscribe = true;
	private boolean running = true;

	public Mqtt(Config cfg, GatewayDiscordClient gateway) {

		this.uri = cfg.getStringOrDefault("mqtt-uri", null);
		this.clientid = cfg.getStringOrDefault("mqtt-client-id", null);
		this.uname = cfg.getStringOrDefault("mqtt-user", null);
		this.pwd = cfg.getStringOrDefault("mqtt-password", null);
		this.logger = new Logger(this.getClass().getSimpleName() + " (" + clientid + ")");

		this.gateway = gateway;
		this.setName(this.logger.getTag());
	}

	public void setClientId(String clientid) {
		this.clientid = clientid;
	}

	public void subscribe(String topic, int qos) throws MqttException {
		this.client.subscribe(topic, qos);
	}

	public void publish(String topic, MqttMessage message) throws MqttException {
		if (this.client == null || !this.client.isConnected()) {
			this.client = new MqttClient(this.uri, this.clientid, new MemoryPersistence());

			MqttConnectOptions ops = new MqttConnectOptions();
			ops.setCleanSession(true);
			if (this.uname != null && this.pwd != null){
				ops.setUserName(this.uname);
				ops.setPassword(this.pwd.toCharArray());
			}

			this.client.connect(ops);
			this.logger.info("Connected to MQTT broker");
		}

		this.client.publish(topic, message);
	}

	@Override
	public void run() {
		try {
			this.client = new MqttClient(this.uri, this.clientid, new MemoryPersistence());

			MqttConnectOptions ops = new MqttConnectOptions();
			ops.setCleanSession(true);
			if (this.uname != null && this.pwd != null){
				ops.setUserName(this.uname);
				ops.setPassword(this.pwd.toCharArray());
			}

			this.client.connect(ops);
			this.logger.info("Connected to MQTT broker");

			if (this.doSubscribe) {
				Map<String, Action> actionMap = new HashMap<>();

				actionMap.put("home/alert", new SendAlertAction());
				actionMap.put("home/sensor", new SensorDataReceivedAction());

				this.client.setCallback(new MqttHandler(this.gateway, actionMap));
				for (String topic : actionMap.keySet())
					this.client.subscribe(topic, 0);

			}

			while (this.running) {}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopThread() {
		this.running = false;
	}

	public void setDoSubscribe(boolean doSubscribe) {
		this.doSubscribe = doSubscribe;
	}
}
