package xyz.rtsvk.alfax.webserver.endpoints;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.webserver.Request;

import java.util.List;

public interface IEndpoint {
	ActionResult handle(GatewayDiscordClient client, Request request);
	byte getRequiredPermissions();
	List<String> getRequiredArgs();
	String getEndpointName();
	List<Request.Method> getAllowedRequestMethods();
}
