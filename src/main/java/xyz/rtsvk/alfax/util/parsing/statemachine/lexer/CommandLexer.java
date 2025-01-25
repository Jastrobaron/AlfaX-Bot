package xyz.rtsvk.alfax.util.parsing.statemachine.lexer;

import xyz.rtsvk.alfax.util.parsing.statemachine.GenericLexicalAnalyzer;
import xyz.rtsvk.alfax.util.parsing.statemachine.LexerException;
import xyz.rtsvk.alfax.util.parsing.statemachine.Predicates;
import xyz.rtsvk.alfax.util.parsing.statemachine.State;

import java.util.List;
import java.util.function.Predicate;

/**
 * Lexical analyzer for the command processor
 * @author Jastrobaron
 */
public class CommandLexer extends GenericLexicalAnalyzer<CommandLexer.CmdPart> {

	/** Non-uoted identifier state */
	private static final String ST_IDENT = "CommandLexer_Ident";
	/** Quoted identifier state */
	private static final String ST_QUOTED_IDENT = "CommandLexer_QuotedIdent";
	/** End of the quoted identifier */
	private static final String ST_QUOTED_END = "CommandLexer_QuotedEnd";
	/** Separator state */
	private static final String ST_SEPARATOR = "CommandLexer_Separator";

	public CommandLexer(Character separator, List<Character> quotes) {
		State<Character> start = getInitialState();
		start.createOutboundState(Predicates.equal(separator), ST_SEPARATOR, true);

		State<Character> quoted = start.createOutboundState(Predicates.equal(quotes), ST_QUOTED_IDENT, false);
		quoted.addTransition(Predicates.anyExcept(quotes));
		quoted.createOutboundState(Predicates.equal(quotes), ST_QUOTED_END, true);

		Predicate<Character> match = Predicates.anyExcept(separator).and(Predicates.anyExcept(quotes));
		State<Character> ident = start.createOutboundState(match, ST_IDENT, true);
		ident.addTransition(Predicates.anyExcept());
	}

	@Override
	protected CmdPart getTokenType(State<Character> currState) {
		if (this.isEndReached()) {     // end of character stream reached
			return CmdPart.CMD_END;
		}

		return switch (currState.getName()) {
			case ST_QUOTED_IDENT -> CmdPart.IDENT;
			case ST_QUOTED_END -> CmdPart.QUOTED_IDENT;
			case ST_SEPARATOR -> CmdPart.SEPARATOR;
			default -> throw new LexerException(currState);
		};
	}

	public enum CmdPart {
		IDENT,
		QUOTED_IDENT,
		SEPARATOR,
		CMD_END
	}
}
