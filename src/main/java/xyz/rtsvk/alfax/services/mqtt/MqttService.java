package xyz.rtsvk.alfax.services.mqtt;

import discord4j.core.GatewayDiscordClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import xyz.rtsvk.alfax.services.Service;
import xyz.rtsvk.alfax.services.mqtt.actions.Action;
import xyz.rtsvk.alfax.services.mqtt.actions.SendAlertAction;
import xyz.rtsvk.alfax.services.mqtt.actions.SensorDataReceivedAction;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * MQTT subscriber service
 * @author Jastrobaron
 */
// FIXME - refactor this mess
public class MqttService extends Service {

	private final Logger logger = new Logger(MqttService.class);

	private GatewayDiscordClient gateway;
	private MqttClient client;

	private final String uri;
	private String clientid;
	private String uname;
	private String pwd;
	private boolean doSubscribe = true;
	private boolean running = true;

	public MqttService(Config cfg, GatewayDiscordClient gateway) {
		super(cfg.getStringOrDefault("mqtt-client-id", null));
		this.clientid = this.getName();

		this.uri = cfg.getStringOrDefault("mqtt-uri", null);
		this.uname = cfg.getStringOrDefault("mqtt-user", null);
		this.pwd = cfg.getStringOrDefault("mqtt-password", null);

		this.gateway = gateway;
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
	protected void startup() throws Exception {
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
	}

	@Override
	protected void loop() throws Exception {
		// nothing to do here
	}

	@Override
	protected void shutdown() throws Exception {
		// nothing to do here
	}

	public void stopThread() {
		this.running = false;
	}

	public void setDoSubscribe(boolean doSubscribe) {
		this.doSubscribe = doSubscribe;
	}
}
