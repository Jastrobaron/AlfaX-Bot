package xyz.rtsvk.alfax.util.statemachine.lexer;

import xyz.rtsvk.alfax.util.statemachine.GenericLexicalAnalyzer;
import xyz.rtsvk.alfax.util.statemachine.Predicates;
import xyz.rtsvk.alfax.util.statemachine.State;

import java.util.List;

/**
 * Lexical analyzer for the command processor
 * @author Jastrobaron
 */
public class CommandLexer extends GenericLexicalAnalyzer<CommandLexer.CmdPart> {

	/** Quoted identifier state */
	private static final String ST_QUOTED_IDENT = "CommandLexer_QuotedIdent";
	/** End of the quoted identifier */
	private static final String ST_QUOTED_END = "CommandLexer_QuotedEnd";
	/** Separator state */
	private static final String ST_SEPARATOR = "CommandLexer_Separator";

	public CommandLexer(Character separator, List<Character> quotes) {
		State<Character> start = getInitialState();
		start.createOutboundState(Predicates.equal(separator), ST_SEPARATOR, true);

		State<Character> quoted = start.createOutboundState(Predicates.anyExcept(quotes), ST_QUOTED_IDENT, false);
		quoted.addTransition(Predicates.anyExcept(quotes));
		quoted.createOutboundState(Predicates.equal(quotes), ST_QUOTED_END, true);
	}

	@Override
	protected CmdPart getTokenType() {
		CmdPart part = switch (this.getCurrentState().getName()) {
			case ST_QUOTED_END -> CmdPart.IDENT;
			case ST_SEPARATOR -> CmdPart.SEPARATOR;
			default -> throw new IllegalStateException("Unexpected state: " + this.getCurrentState().getName());
		};

		return part;
	}

	public enum CmdPart {
		IDENT,
		QUOTED_IDENT,
		SEPARATOR,
		CMD_END
	}
}
