package xyz.rtsvk.alfax.util.parsing.statemachine.lexer;

import xyz.rtsvk.alfax.util.parsing.statemachine.GenericLexicalAnalyzer;
import xyz.rtsvk.alfax.util.parsing.statemachine.LexerException;
import xyz.rtsvk.alfax.util.parsing.statemachine.State;

public class JsonLexer extends GenericLexicalAnalyzer<JsonLexer.JsonLexemeType> {

	private static final String ST_JSON_COLON = "JsonLexer_Colon";
	private static final String ST_JSON_COMMA = "JsonLexer_Comma";
	private static final String ST_JSON_LEFT_BRACE = "JsonLexer_LeftBrace";
	private static final String ST_JSON_RIGHT_BRACE = "JsonLexer_RightBrace";
	private static final String ST_JSON_LEFT_SQ_BRACE = "JsonLexer_LeftSquareBrace";
	private static final String ST_JSON_RIGHT_SQ_BRACE = "JsonLexer_RightSquareBrace";

	public JsonLexer() {
		getInitialState().createOutboundState(':', ST_JSON_COLON, true);
		getInitialState().createOutboundState(',', ST_JSON_COMMA, true);
		getInitialState().createOutboundState('{', ST_JSON_LEFT_BRACE, true);
		getInitialState().createOutboundState('}', ST_JSON_RIGHT_BRACE, true);
		getInitialState().createOutboundState('[', ST_JSON_LEFT_SQ_BRACE, true);
		getInitialState().createOutboundState(']', ST_JSON_RIGHT_SQ_BRACE, true);


	}

	@Override
	protected JsonLexemeType getTokenType(State<Character> state) {
		return switch (state.getName()) {
			case ST_JSON_COLON -> JsonLexemeType.COLON;
			case ST_JSON_COMMA -> JsonLexemeType.COMMA;
			case ST_JSON_LEFT_BRACE -> JsonLexemeType.LEFT_BRACE;
			case ST_JSON_RIGHT_BRACE -> JsonLexemeType.RIGHT_BRACE;
			case ST_JSON_LEFT_SQ_BRACE -> JsonLexemeType.LEFT_SQ_BRACE;
			case ST_JSON_RIGHT_SQ_BRACE -> JsonLexemeType.RIGHT_SQ_BRACE;
			default -> throw new LexerException(state);
		};
	}

	public enum JsonLexemeType {
		STRING,
		INT,
		FLOAT,
		COLON,
		COMMA,
		LEFT_BRACE,
		RIGHT_BRACE,
		LEFT_SQ_BRACE,
		RIGHT_SQ_BRACE
	}
}
