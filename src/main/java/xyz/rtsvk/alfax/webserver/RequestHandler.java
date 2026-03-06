package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.mqtt.Mqtt;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.webserver.actions.*;
import xyz.rtsvk.alfax.webserver.contentparsing.Content;
import xyz.rtsvk.alfax.webserver.contentparsing.FormContent;
import xyz.rtsvk.alfax.webserver.contentparsing.JsonContent;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler implements Runnable {

	private final Socket skt;

	private final BufferedInputStream in;
	private final PrintWriter out;
	private final GatewayDiscordClient client;
	private final Logger logger;

	private final Map<String, Content> supportedContentTypes;
	private final List<ActionData> actions;
	private final Config cfg;

	public RequestHandler(Config cfg, Mqtt mqtt, Socket s, GatewayDiscordClient client) throws IOException {
		this.cfg = cfg;
		this.skt = s;
		this.client = client;
		this.logger = new Logger(this.getClass());

		this.in = new BufferedInputStream(s.getInputStream());
		this.out = new PrintWriter(s.getOutputStream());

		this.supportedContentTypes = new HashMap<>();
		this.supportedContentTypes.put("application/json", new JsonContent());
		this.supportedContentTypes.put("application/x-www-form-urlencoded", new FormContent());

		this.actions = new ArrayList<>();
		this.actions.add(new ActionData(
				"channel_message",
							new SendMessageAction(),
							Database.PERMISSION_API_CHANNEL,
							List.of("message", "channel_id")));
		this.actions.add(new ActionData(
				"direct_message",
							new DirectMessageAction(),
							Database.PERMISSION_API_DM,
							List.of("message", "user_id")));
		if (cfg.getBooleanOrDefault("mqtt-enabled", false))
			this.actions.add(new ActionData(
					"mqtt_publish",
								new MqttPublishAction(mqtt),
								Database.PERMISSION_MQTT,
								List.of("topic", "message")));
	}

	@Override
	public void run() {
		try {
			this.logger.info("Incoming request from " + this.skt.getRemoteSocketAddress());
			this.skt.setSoTimeout(10000);
			Request request = Request.parseRequest(this.in, this.supportedContentTypes);

			if (request == null) {
				this.out.println("HTTP/1.1 " + Response.RESP_400_BAD_REQUEST);
				this.out.close();
				this.in.close();
				this.skt.close();
				return;
			}

			String message = "Server responeded with an error";
			String key = request.getProperty("auth_key", "").toString();
			this.out.print(request.getProtocolVersion() + " ");
			ActionData actionData = this.actions.stream()
					.filter(a -> a.getEndpointName().equals(request.getPath().substring(1)))
					.findFirst().orElse(null);

			if (!request.getRequestMethod().equals("POST"))
				this.out.println(request.getProtocolVersion() + " " + Response.RESP_501_NOT_IMPLEMENTED);

			else if (actionData == null) {
				this.out.println(Response.RESP_404_NOT_FOUND);
			}
			else if (!request.hasProperty("auth_key")) {
				this.out.println(Response.RESP_401_UNAUTHORIZED);
				message = "Missing auth_key parameter";
			}
			else if (!Database.checkPermissionsByKey(key, actionData.getRequiredPermissions())) {
				this.out.println(Response.RESP_403_FORBIDDEN);
				message = "You don't have permissions to do that";
			}
			else if (actionData.getRequiredArgs().stream().anyMatch(a -> !request.hasProperty(a))) {
				this.out.println(Response.RESP_400_BAD_REQUEST);
				message = "Missing parameters";
			}
			else {
				Action action = actionData.getAction();
				ActionResult result = action.handle(this.client, request);
				this.out.println(result.getStatus());
				message = result.getMessage();
			}

			/*StringBuilder message = new StringBuilder("Lorem ipsum dolor sit amet.\n");
			if (request != null) request.getProperties().forEach((k,v) -> message.append(k + " = " + v + "\n"));*/

			this.out.println("Content-type: text/plain");
			this.out.println("Content-length: " + message.length());
			this.out.println();
			this.out.println(message);
			this.out.println();

			this.out.close();
			this.in.close();
			this.skt.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
