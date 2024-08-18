package xyz.rtsvk.alfax.webserver;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.endpoints.ActionResult;

import java.util.List;

public interface IEndpoint {
	ActionResult handle(GatewayDiscordClient client, Request request);
	byte getRequiredPermissions();
	List<String> getRequiredArgs();
	String getEndpointName();
	List<Request.Method> getAllowedRequestMethods();
}
