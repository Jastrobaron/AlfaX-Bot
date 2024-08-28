package xyz.rtsvk.alfax.services.webserver.endpoints;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.services.webserver.IEndpoint;
import xyz.rtsvk.alfax.services.webserver.Request;

import java.util.List;

public class EditMessageEndpoint implements IEndpoint {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		Snowflake messageId = Snowflake.of(request.getProperty("message_id").toString());
		Snowflake channelId = Snowflake.of(request.getProperty("channel_id").toString());
		String content = request.getProperty("content").toString();
		if (content.isEmpty()) {
			return ActionResult.badRequest("Empty content is not allowed!");
		}
		Message msg = client.getMessageById(channelId, messageId).block();
		if (msg == null){
			return ActionResult.notFound("Message not found");
		}
		msg.edit(spec -> spec.setContent(content)).block();
		return ActionResult.ok("Message edited!");
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_API;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of("message_id", "channel_id", "content");
	}

	@Override
	public String getEndpointName() {
		return "/edit_message";
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.POST);
	}
}
