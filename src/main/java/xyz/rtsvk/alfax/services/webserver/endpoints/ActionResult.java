package xyz.rtsvk.alfax.services.webserver.endpoints;

import xyz.rtsvk.alfax.services.webserver.Response;

public record ActionResult(String status, String contentType, String message) {

	public static ActionResult ok() {
		return ok("Message sent");
	}

	public static ActionResult ok(String message) {
		return ok("text/html", message);
	}

	public static ActionResult ok(String contentType, String message) {
		return new ActionResult(Response.OK, contentType, message);
	}

	public static ActionResult internalError() {
		return internalError("Internal server error");
	}

	public static ActionResult internalError(String message) {
		return new ActionResult(Response.ERROR, "text/html", message);
	}

	public static ActionResult notFound() {
		return notFound("Specified endpoint not found.");
	}

	public static ActionResult notFound(String message) {
		return new ActionResult(Response.NOT_FOUND,"text/html", message);
	}

	public static ActionResult badRequest() {
		return badRequest("Chlope nerozumim ci ani pol chuja.");
	}

	public static ActionResult badRequest(String message) {
		return new ActionResult(Response.BAD_REQUEST,"text/html", message);
	}

	public static ActionResult unauthorized() {
		return unauthorized("Access denied!");
	}

	public static ActionResult unauthorized(String message) {
		return new ActionResult(Response.UNAUTHORIZED,"text/html", message);
	}

	public static ActionResult notImplemented() {
		return notImplemented("Request method not implemented");
	}

	public static ActionResult notImplemented(String message) {
		return new ActionResult(Response.NOT_IMPLEMENTED,"text/html", message);
	}

	public static ActionResult forbidden() {
		return forbidden("You don't have permissions to do that");
	}

	public static ActionResult forbidden(String message) {
		return new ActionResult(Response.FORBIDDEN,"text/html", message);
	}
}
