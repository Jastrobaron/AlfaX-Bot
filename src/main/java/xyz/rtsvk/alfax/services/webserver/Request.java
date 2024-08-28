package xyz.rtsvk.alfax.services.webserver;

import xyz.rtsvk.alfax.util.parsing.IParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Request {

	private final Method requestMethod;
	private final String path;
	private final String protocolVersion;

	private final Map<String, Object> requestHeader;
	private final Map<String, Object> requestBody;

	public Request(Header header, Map<String, Object> body) {
		this.requestMethod = header.getRequestMethod();
		this.path = header.getPath();
		this.protocolVersion = header.getProtocolVersion();
		this.requestHeader = header.getHeaderData();
		this.requestBody = body;
	}

	public static Request parseRequest(BufferedInputStream in, Map<String, IParser> supportedContentTypes) throws Exception {
		StreamOutput streamOutput = readStreamUntil(in, "\n\n");
		String raw = streamOutput.getData();
		Header header = parseRequestHeader(raw);
		if (header == null) return null;

		String contentType = header.getHeaderProperty("content-type", "undefined").toString();
		int length = Integer.parseInt(header.getHeaderProperty("content-length", 0).toString());

		int read;
		byte[] buf = new byte[64];
		StringBuilder sb = new StringBuilder(streamOutput.getRemainder());
		while (sb.length() < length && (read = in.read(buf)) > 0) {
			sb.append(new String(buf, 0, read));
		}

		IParser c = supportedContentTypes.get(contentType);
		if (c != null && length > 0 && !sb.isEmpty()) {
			return new Request(header, c.parse(sb.toString()));
		} else {
			return new Request(header, null);
		}
	}

	public static Header parseRequestHeader(String raw) {
		try {
			String[] lines = raw.split("\n");
			String[] head = lines[0].split(" ");

			Header header = new Header(head[0], head[1], head[2]);

			for (int i = 1; i < lines.length; i++) {
				lines[i] = lines[i].replace(" ", "");
				lines[i] = lines[i].toLowerCase();

				int endIdx = lines[i].indexOf(";");
				endIdx = endIdx > 0 ? endIdx : lines[i].length();

				int colIdx = lines[i].indexOf(":");
				if (colIdx < 0) continue;

				header.headerData.put(
						lines[i].substring(0, colIdx),
						lines[i].substring(colIdx+1, endIdx)
				);
			}
			return header;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static StreamOutput readStreamUntil(BufferedInputStream in, String delimiter) throws IOException {
		StringBuilder sb = new StringBuilder();
		int delimiterLength = delimiter.length();

		String data = "";
		do {
			byte[] buf = new byte[64];
			int read = in.read(buf);
			data = new String(buf, 0, read, StandardCharsets.UTF_8);
			data = data.replace("\r", "");
			sb.append(data);
		}
		while (!data.contains(delimiter));

		StreamOutput out = new StreamOutput();
		String raw = sb.toString();
		out.data = raw.substring(0, raw.indexOf(delimiter));
		out.remainder = data.substring(data.indexOf(delimiter) + delimiterLength);
		return out;
	}

	public Request.Method getRequestMethod() {
		return requestMethod;
	}

	public String getPath() {
		return path;
	}

	public String getProtocolVersion() {
		return protocolVersion;
	}

	public Object getHeaderProperty(String key) {
		return this.requestHeader.get(key);
	}

	public Object getHeaderProperty(String key, Object def) {
		return this.requestHeader.getOrDefault(key, def);
	}

	public Map<String, Object> getHeaders() {
		return this.requestHeader;
	}

	public boolean hasProperty(String key) {
		return this.requestBody.containsKey(key);
	}

	public Object getProperty(String key) {
		return this.requestBody.get(key);
	}

	public Optional<Object> getOptionalProperty(String key) {
		return Optional.ofNullable(getProperty(key));
	}

	public Object getPropertyOrDefault(String key, Object def) {
		return this.requestBody.getOrDefault(key, def);
	}

	public String getPropertyAsString(String key) {
		if (!this.requestBody.containsKey(key)) {
			return null;
		}
		return String.valueOf(this.requestBody.get(key));
	}

	public Map<String, Object> getProperties() {
		return this.requestBody;
	}

	public static class Header {

		private final Map<String, Object> headerData;
		private final Method requestMethod;
		private final String path;
		private final String protocolVersion;

		public Header(String requestMethod, String path, String protocolVersion) {
			this.headerData = new HashMap<>();
			this.requestMethod = Method.valueOf(requestMethod);
			this.path = path;
			this.protocolVersion = protocolVersion;
		}

		public Map<String, Object> getHeaderData() {
			return headerData;
		}

		public Object getHeaderProperty(String key) {
			return this.headerData.get(key);
		}

		public Object getHeaderProperty(String key, Object def) {
			return this.headerData.getOrDefault(key, def);
		}

		public Request.Method getRequestMethod() {
			return requestMethod;
		}

		public String getPath() {
			return path;
		}

		public String getProtocolVersion() {
			return protocolVersion;
		}
	}

	private static class StreamOutput {

		private String data;
		private String remainder;

		public String getData() {
			return data;
		}

		public String getRemainder() {
			return remainder;
		}
	}

	public enum Method {
		POST,
		GET,
		PUT,
		HEAD,
		DELETE
	}
}
