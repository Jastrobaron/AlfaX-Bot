package xyz.rtsvk.alfax.webserver.endpoints;

import discord4j.core.GatewayDiscordClient;
import xyz.rtsvk.alfax.util.Database;
import xyz.rtsvk.alfax.util.FileManager;
import xyz.rtsvk.alfax.webserver.IEndpoint;
import xyz.rtsvk.alfax.webserver.Request;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetFileEndpoint implements IEndpoint {

	private Map<String, String> contentTypes;

	public GetFileEndpoint() {
		this.contentTypes = new HashMap<>();
		this.contentTypes.put("html", "text/html");
		this.contentTypes.put("txt", "text/plain");
		this.contentTypes.put("mp3", "audio/mpeg");
		this.contentTypes.put("json", "application/json");
	}

	@Override
	public ActionResult handle(GatewayDiscordClient client, Request request) {
		File file = new File(FileManager.WEB_ROOT, request.getPath());
		if (file.isDirectory()) {
			String path = request.getPath().endsWith("/") ? request.getPath() : request.getPath() + "/";
			file = new File(FileManager.WEB_ROOT, path + "index.html");
		}

		if (!file.canRead()) {
			return ActionResult.forbidden("File cannot be read!");
		}
		String path = file.getPath();
		String contentType = this.contentTypes.getOrDefault(path.substring(path.lastIndexOf(".")), "text/plain");

		try (InputStream fileInput = new FileInputStream(file)) {
			StringBuilder content = new StringBuilder();
			byte[] buffer = new byte[64];
			int read;
			while ((read = fileInput.read(buffer)) != -1) {
				content.append(new String(buffer, 0, read));
			}

			return ActionResult.ok(contentType, content.toString());
		}
		catch (FileNotFoundException e) {
			return ActionResult.notFound(e.getMessage());
		}
		catch (IOException e) {
			return ActionResult.internalError(e.getMessage());
		}
	}

	@Override
	public byte getRequiredPermissions() {
		return Database.PERMISSION_API_GET_FILE;
	}

	@Override
	public List<String> getRequiredArgs() {
		return List.of();
	}

	@Override
	public String getEndpointName() {
		return "storage/*";
	}

	@Override
	public List<Request.Method> getAllowedRequestMethods() {
		return List.of(Request.Method.GET);
	}
}
