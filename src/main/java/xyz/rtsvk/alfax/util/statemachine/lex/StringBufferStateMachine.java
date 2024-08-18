package xyz.rtsvk.alfax.util.statemachine.lex;

import xyz.rtsvk.alfax.util.statemachine.StateMachine;

/**
 * Class utilising a {@link StringBuffer} to store the token currently being created
 * @author Jastrobaron
 */
public class StringBufferStateMachine extends StateMachine<Character, String> {

    /** Buffer to store characters that have been read */
    private final StringBuffer buffer;

    /**
     * Class constructor
     */
    public StringBufferStateMachine() {
        super();
        this.buffer = new StringBuffer();
    }

    @Override
    protected void onTransition(Character edge) {
        this.buffer.append(edge);
    }

    @Override
    protected String getResult() {
        return this.buffer.toString();
    }

    @Override
    public void reset() {
        super.reset();
        this.buffer.setLength(0);
    }
}
