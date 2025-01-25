package xyz.rtsvk.alfax.util.parsing.statemachine;

/**
 * Class utilising a {@link StringBuffer} to store the token currently being created
 * @param <T> lexeme type enum
 * @author Jastrobaron
 */
public abstract class GenericLexicalAnalyzer<T> extends StateMachine<Character, Lexeme<T>> {

    /** Buffer to store characters that have been read */
    private final StringBuffer buffer;

    /**
     * Class constructor
     */
    public GenericLexicalAnalyzer() {
        this.buffer = new StringBuffer();
    }

    @Override
    protected void onTransition(Character edge) {
        this.buffer.append(edge);
    }

    @Override
    protected Lexeme<T> getResult() {
        return new Lexeme<>(getTokenType(this.getCurrentState()), this.buffer.toString());
    }

    @Override
    public void reset() {
        super.reset();
        this.buffer.setLength(0);
    }

    /**
     * Returns the token type based on the state the automaton finished in
     * @param state state the automaton finished in
     * @return the token (lexeme) type
     */
    protected abstract T getTokenType(State<Character> state);
}
