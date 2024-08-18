package xyz.rtsvk.alfax.util.parsing.kv;

import xyz.rtsvk.alfax.util.parsing.IParser;
import xyz.rtsvk.alfax.util.statemachine.impl.KeyValueStateMachine;

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
			if (!in.markSupported()) {
				throw new Exception("Marking is not supported for the type of InputStream used!");
			}
			KeyValueStateMachine fsm = new KeyValueStateMachine(this.entrySeparator);
			fsm.reset();
			int c;
			while ((c = in.read()) != -1) {
				in.mark(1);
				if (!fsm.transition((char) c)) {
					continue;
				}
				in.reset();
				String entry = fsm.getBufferContent();
				int kvDelimIdx = entry.indexOf(KeyValueStateMachine.KEY_VALUE_DELIMITER);
				String key = entry.substring(0, kvDelimIdx);
				Object value = entry.substring(kvDelimIdx + 1).trim();
				result.put(key, value);
			}
		}
		return result;
	}
}
