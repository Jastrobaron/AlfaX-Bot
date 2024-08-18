package xyz.rtsvk.alfax.util.parsing.json;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.rtsvk.alfax.webserver.IContent;

import java.util.Map;

public class JsonContent implements IContent {
	@Override
	public Map<String, Object> parse(String s) {
		try {
			JSONParser parser = new JSONParser();
			return (Map<String, Object>) parser.parse(s);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
