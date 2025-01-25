package xyz.rtsvk.alfax.util.parsing.topdown.json;

import xyz.rtsvk.alfax.util.parsing.topdown.IParser;
import xyz.rtsvk.alfax.util.parsing.statemachine.Inputs;
import xyz.rtsvk.alfax.util.parsing.statemachine.lexer.JsonLexer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used for parsing JSON data
 * @author Jastrobaron
 */
public class JsonParser implements IParser {

	private JsonLexer lexer;

	public JsonParser() {
		this.lexer = new JsonLexer();
	}

	@Override
	public Map<String, Object> parse(Object source) {
		if (source instanceof String rawData) {
			this.lexer.setInput(Inputs.fromString(rawData));
		} else if (source instanceof InputStream in) {
			this.lexer.setInput(Inputs.fromInputStream(in));
		}

		if (!this.lexer.hasInput()) {
			throw new IllegalStateException("Lexical analyzer input is not set!");
		}

		Map<String, Object> result = new HashMap<>();
		Start(result);
		return result;
	}

	private void Start(Map<String, Object> treeNode) {

	}
}
