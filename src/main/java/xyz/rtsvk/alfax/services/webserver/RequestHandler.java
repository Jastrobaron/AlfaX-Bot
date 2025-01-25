package xyz.rtsvk.alfax.services.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.services.webserver.endpoints.ActionResult;
import xyz.rtsvk.alfax.services.webserver.endpoints.EndpointNotFoundEndpoint;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.parsing.topdown.IParser;
import xyz.rtsvk.alfax.util.parsing.topdown.json.JsonParser;
import xyz.rtsvk.alfax.util.parsing.topdown.kv.URLEncodedParser;
import xyz.rtsvk.alfax.util.text.FormattedString;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler implements Runnable {

	private static final Map<String, IParser> SUPPORTED_FORMATS = new HashMap<>();

	static {
		SUPPORTED_FORMATS.put("application/x-www-form-urlencoded", new URLEncodedParser());
		SUPPORTED_FORMATS.put("application/json", new JsonParser());
	}

	private final Socket httpClient;
	private final BufferedInputStream in;
	private final PrintWriter out;
	private final GatewayDiscordClient client;
	private final Logger logger;

	private final List<IEndpoint> actions;
	private final Config cfg;
	private final int timeout;

	public RequestHandler(WebServerService server, Socket httpClient, int timeout) throws IOException {
		this.logger = new Logger(this.getClass());
		this.cfg = server.getConfig();
		this.client = server.getDiscordClient();
		this.actions = server.getEndpoints();
		this.timeout = timeout;

		this.httpClient = httpClient;
		this.in = new BufferedInputStream(httpClient.getInputStream());
		this.out = new PrintWriter(httpClient.getOutputStream());
	}

	@Override
	public void run() {
		try {
			this.httpClient.setSoTimeout(this.timeout);
			Request request = Request.parseRequest(this.in, SUPPORTED_FORMATS);

			if (request == null) {
				this.out.println("HTTP/1.1 " + Response.BAD_REQUEST);
				this.out.close();
				this.in.close();
				this.httpClient.close();
				return;
			}

			this.logger.info(FormattedString.create("Incoming request from ${host}: ${method} ${path} ${version}")
					.addParam("host", this.httpClient.getRemoteSocketAddress())
					.addParam("method", request.getRequestMethod())
					.addParam("path", request.getPath())
					.addParam("version", request.getProtocolVersion())
					.build());

			ActionResult result;
			String key = request.getPropertyAsString("auth_key");
			String path = request.getPath();
			IEndpoint action = this.actions.stream()
					.filter(a -> TextUtils.match(path, a.getEndpointName()))
					.findFirst().orElse(new EndpointNotFoundEndpoint());

			if (key == null) {
				result = ActionResult.unauthorized("Missing auth_key parameter!");
			}
			else if (!action.getAllowedRequestMethods().contains(request.getRequestMethod())) {
				String message = TextUtils.format("Request method '${0}' is not allowed for endpoint ${1}!", request.getRequestMethod(), request.getPath());
				result = ActionResult.notImplemented(message);
			}
			else if (!Database.checkPermissionsByKey(key, action.getRequiredPermissions())) {
				result = ActionResult.forbidden();
			}
			else if (action.getRequiredArgs().stream().anyMatch(a -> !request.hasProperty(a))) {
				List<String> missingArgs = action.getRequiredArgs().stream().filter(e -> !request.hasProperty(e)).toList();
				result = ActionResult.badRequest("Missing parameters " + String.join(", ", missingArgs));
			}
			else {
				result = action.handle(this.client, request);
			}

			this.logger.info(FormattedString.create("Response to ${host}: ${status}: ${message}")
					.addParam("host", this.httpClient.getRemoteSocketAddress())
					.addParam("status", result.status())
					.addParam("message", result.message())
					.build());

			this.out.println(request.getProtocolVersion() + " " + result.status());
			this.out.println("Content-type: text/html");
			this.out.println("Content-length: " + result.message().length());
			this.out.println();
			this.out.println(result.message());
			this.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				this.out.close();
				this.in.close();
				this.httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
