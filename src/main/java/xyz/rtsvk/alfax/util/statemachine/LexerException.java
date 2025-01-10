package xyz.rtsvk.alfax.util.statemachine;

/**
 * Exception to be thrown when a lexical error occurs
 * @author Jastrobaron
 */
public class LexerException extends IllegalStateException {
	/**
	 * Constructor
	 * @param state the lexical analyzer finished in
	 */
	public LexerException(State<?> state) {
		super("Lexical analysis finished in non-finite state " + state.getName());
	}
}
