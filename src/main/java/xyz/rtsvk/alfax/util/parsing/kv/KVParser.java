package xyz.rtsvk.alfax.util.parsing.kv;

import xyz.rtsvk.alfax.util.parsing.IParser;
import xyz.rtsvk.alfax.util.statemachine.StateMachine;
import xyz.rtsvk.alfax.util.statemachine.TransitionResult;
import xyz.rtsvk.alfax.util.statemachine.lex.KeyValueStateMachine;
import xyz.rtsvk.alfax.util.statemachine.input.InputSuppliers;
import xyz.rtsvk.alfax.util.statemachine.parsers.KVParserStateMachine;
import xyz.rtsvk.alfax.util.tuples.Pair;

import java.io.InputStream;
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
		if (source instanceof String raw) {
			for (String entry : raw.split(this.entrySeparator.toString())) {
				int kvDelimIdx = entry.indexOf(KeyValueStateMachine.KEY_VALUE_DELIMITER);
				String key = entry.substring(0, kvDelimIdx);
				Object value = entry.substring(kvDelimIdx + 1).trim();
				result.put(key, value);
			}
		} else if (source instanceof InputStream in) {
			KeyValueStateMachine lexer = new KeyValueStateMachine(this.entrySeparator);
			lexer.setInputSupplier(InputSuppliers.fromInputStream(in));
			lexer.reset();

			KVParserStateMachine parser = new KVParserStateMachine(this.entrySeparator.toString());
			parser.setInputSupplier(InputSuppliers.fromStateMachine(lexer));
			parser.reset();

			Pair<String, String> kvPair;
			while ((kvPair = parser.getNext()) != null) {
				result.put(kvPair.getKey(), kvPair.getValue());
			}
		}
		return result;
	}
}
