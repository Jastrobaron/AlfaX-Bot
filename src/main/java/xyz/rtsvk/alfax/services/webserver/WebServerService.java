package xyz.rtsvk.alfax.services.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.services.Service;
import xyz.rtsvk.alfax.services.mqtt.MqttService;
import xyz.rtsvk.alfax.services.webserver.endpoints.*;
import xyz.rtsvk.alfax.util.Config;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Webserver service
 * @author Jastrobaron
 */
public class WebServerService extends Service {

	/** Discord client */
	private final GatewayDiscordClient client;
	/** Startup config */
	private final Config cfg;
	/** MQTT client */
	private MqttService mqtt;
	/** List of endpoints */
	private List<IEndpoint> endpoints;
	/** Server socket to listen for incoming connections */
	private ServerSocket server;
	/** Timeout interval in milliseconds */
	private int timeout;

	public WebServerService(Config cfg, GatewayDiscordClient client) {
		super("WebServer");
		this.cfg = cfg;
		this.client = client;
	}

	@Override
	protected void startup() throws Exception {
		this.endpoints = new ArrayList<>();
		this.endpoints.add(new ChannelMessageEndpoint());
		this.endpoints.add(new DirectMessageEndpoint());
		this.endpoints.add(new GetFileEndpoint());
		this.endpoints.add(new EditMessageEndpoint());

		if (this.cfg.getBoolean("mqtt-enabled")) {
			this.mqtt = new MqttService(cfg, client);
			this.mqtt.setClientId(cfg.getString("mqtt-web-client-id"));
			this.endpoints.add(new MqttPublishEndpoint(this.mqtt));
		}
		else {
			this.mqtt = null;
		}

		this.timeout = this.cfg.getInt("webserver-timeout-ms");
		this.server = new ServerSocket(this.cfg.getInt("webserver-port"));
	}

	@Override
	protected void loop() throws Exception {
		Socket httpClient = this.server.accept();
		Thread handler = new Thread(new RequestHandler(this, httpClient, this.timeout));
		handler.start();
	}

	@Override
	protected void shutdown() throws Exception {
		this.server.close();
		this.endpoints.clear();
	}

	public GatewayDiscordClient getDiscordClient() {
		return this.client;
	}

	public Config getConfig() {
		return this.cfg;
	}

	public MqttService getMqtt() {
		return this.mqtt;
	}

	public List<IEndpoint> getEndpoints() {
		return this.endpoints;
	}
}
