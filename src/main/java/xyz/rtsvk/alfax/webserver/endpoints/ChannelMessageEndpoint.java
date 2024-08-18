package xyz.rtsvk.alfax.webserver.endpoints;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateMono;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.webserver.IEndpoint;
import xyz.rtsvk.alfax.webserver.Request;

import java.util.List;

public class ChannelMessageEndpoint implements IEndpoint {
	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {

		String channelID = request.getProperty("channel_id").toString();
		String msg = request.getProperty("message").toString();

		try {
			MessageChannel channel = (MessageChannel) client.getChannelById(Snowflake.of(channelID)).block();
			MessageCreateMono msgCreation = channel.createMessage(msg);
			if (request.hasProperty("ref")) {
				msgCreation = msgCreation.withMessageReference(Snowflake.of(request.getPropertyAsString("ref")));
			}
			msgCreation.block();
		} catch (Exception e) {
			String error = e.getClass().getSimpleName() + '\n' + e.getMessage() + '\n';
			return ActionResult.internalError(error);
		}

		return ActionResult.ok();
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_API_CHANNEL;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of("message", "channel_id");
	}

	@Override
	public String getEndpointName() {
		return "/channel_message";
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.POST);
	}
}
