package xyz.rtsvk.alfax.services.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.services.webserver.endpoints.ActionResult;

import java.util.List;

public interface IEndpoint {
	ActionResult handle(GatewayDiscordClient client, Request request);
	byte getRequiredPermissions();
	List<String> getRequiredArgs();
	String getEndpointName();
	List<Request.Method> getAllowedRequestMethods();
}
