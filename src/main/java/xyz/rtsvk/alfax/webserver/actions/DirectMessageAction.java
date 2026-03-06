package xyz.rtsvk.alfax.webserver.actions;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import xyz.rtsvk.alfax.webserver.Request;
import xyz.rtsvk.alfax.webserver.Response;

public class DirectMessageAction implements Action {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String userID = request.getProperty("user_id").toString();
		String msg = request.getProperty("message").toString();
		if (msg.isEmpty())
			return new ActionResult(Response.RESP_400_BAD_REQUEST, "Message is empty");

		User user = client.getUserById(Snowflake.of(userID)).block();
		if (user == null)
			return new ActionResult(Response.RESP_404_NOT_FOUND, "User not found");

		PrivateChannel channel = user.getPrivateChannel().block();
		if (channel == null)
			return new ActionResult(Response.RESP_404_NOT_FOUND, "Unable to open DM channel");

		try {
			channel.createMessage(msg).block();
		} catch (Exception e) {
			return new ActionResult(Response.RESP_500_ERROR, e.getMessage());
		}

		return new ActionResult(Response.RESP_200_OK, "Message sent");
	}
}
