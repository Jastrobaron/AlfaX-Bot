package xyz.rtsvk.alfax.util.statemachine;

/**
 * Class utilising a {@link StringBuffer} to store the token currently being created
 * @author Jastrobaron
 */
public abstract class GenericLexicalAnalyzer<T> extends StateMachine<Character, Token<T>> {

    /** Buffer to store characters that have been read */
    private final StringBuffer buffer;

    /**
     * Class constructor
     */
    public GenericLexicalAnalyzer() {
        super();
        this.buffer = new StringBuffer();
    }

    @Override
    protected void onTransition(Character edge) {
        this.buffer.append(edge);
    }

    @Override
    protected Token<T> getResult() {
        return new Token<>(getTokenType(), this.buffer.toString());
    }

    @Override
    public void reset() {
        super.reset();
        this.buffer.setLength(0);
    }

    protected abstract T getTokenType();
}
