package xyz.rtsvk.alfax.services.webserver.endpoints;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.text.TextUtils;
import xyz.rtsvk.alfax.services.webserver.IEndpoint;
import xyz.rtsvk.alfax.services.webserver.Request;

import java.util.List;

public class EndpointNotFoundEndpoint implements IEndpoint {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {
		return ActionResult.notFound(TextUtils.format("Path '${0}' was not found!", request.getPath()));
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_NONE;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of();
	}

	@Override
	public String getEndpointName() {
		return null;
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.values());
	}
}
