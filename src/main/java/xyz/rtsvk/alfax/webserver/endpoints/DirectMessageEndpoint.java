package xyz.rtsvk.alfax.webserver.endpoints;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.webserver.IEndpoint;
import xyz.rtsvk.alfax.webserver.Request;

import java.util.List;

public class DirectMessageEndpoint implements IEndpoint {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String userID = request.getProperty("user_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.isEmpty())
			return ActionResult.badRequest("Message is empty");

		User user = client.getUserById(Snowflake.of(userID)).block();
		if (user == null)
			return ActionResult.notFound("User not found");

		PrivateChannel channel = user.getPrivateChannel().block();
		if (channel == null)
			return ActionResult.notFound("Unable to open DM channel");

		try {
			channel.createMessage(msg).block();
		} catch (Exception e) {
			return ActionResult.internalError(e.getMessage());
		}

		return ActionResult.ok();
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_API_DM;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of("user_id", "message");
	}

	@Override
	public String getEndpointName() {
		return "/direct_message";
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.POST);
	}
}
