package xyz.rtsvk.alfax.util.parsing.json;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.rtsvk.alfax.util.parsing.IParser;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Class used for parsing JSON data
 * @author Jastrobaron
 */
public class JsonParser implements IParser {

	private static final JSONParser parser = new JSONParser();

	@Override
	public Map<String, Object> parse(Object source) {
		try {
			if (source instanceof String rawData) {
				return (Map<String, Object>) parser.parse(rawData);
			} else if (source instanceof Reader in) {
				return (Map<String, Object>) parser.parse(in);
			} else {
				return Map.of();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return Map.of();
		}
	}
}
