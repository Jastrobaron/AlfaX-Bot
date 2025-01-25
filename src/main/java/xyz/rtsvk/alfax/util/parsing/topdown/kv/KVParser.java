package xyz.rtsvk.alfax.util.parsing.topdown.kv;

import xyz.rtsvk.alfax.util.parsing.topdown.IParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic parser of key-value data format
 * @author Jastrobaron
 */
public class KVParser implements IParser {

	/** Character used as a delimiter between all key-value pairs */
	private final Character entrySeparator;

	/**
	 * Class constructor
	 * @param entrySeparator character to separate key-value entries
	 */
	public KVParser(Character entrySeparator) {
		this.entrySeparator = entrySeparator;
	}

	@Override
	public Map<String, Object> parse(Object source) throws Exception {
		Map<String, Object> result = new HashMap<>();

		return result;
	}
}
