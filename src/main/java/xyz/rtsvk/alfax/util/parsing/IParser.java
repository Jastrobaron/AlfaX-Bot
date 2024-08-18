package xyz.rtsvk.alfax.util.parsing;

import java.util.Map;

/**
 * Generic parser interface
 * @author Jastrobaron
 */
public interface AParser {
	/**
	 * Method to parse into a key-value map
	 * @param source of raw non-parsed data
	 * @return data parsed into a key-value map
	 */
	Map<String, Object> parse(Object source);
}
