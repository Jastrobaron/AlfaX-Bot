package xyz.rtsvk.alfax.util.parsing.topdown.kv;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for parsing URL encoded data
 */
public class URLEncodedParser extends KVParser {

	/**
	 * Class constructor
	 */
	public URLEncodedParser() {
		super('&');
	}

	@Override
	public Map<String, Object> parse(Object obj) throws Exception {
		Map<String, Object> result = new HashMap<>();
		if (obj instanceof String s) {
			int index = 0;
			while (index < s.length()) {
				char currChar = s.charAt(index);
				if (currChar == '%') {
					String hex = s.substring(index + 1, index + 3);
					char ch = (char) Integer.parseInt(hex, 16);
					s = s.substring(0, index) + ch + s.substring(index + 3);
				}
				else if (currChar == '+') {
					s = s.substring(0, index) + ' ' + s.substring(index + 1);
				}
			}
			result.putAll(super.parse(s));
		}
		return result;
	}
}
