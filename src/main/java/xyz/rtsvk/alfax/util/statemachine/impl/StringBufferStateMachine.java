package xyz.rtsvk.alfax.util.statemachine.impl;

import xyz.rtsvk.alfax.util.statemachine.StateMachine;

/**
 * Class utilising a {@link StringBuffer} to store the token currently being created
 * @author Jastrobaron
 */
public class StringBufferStateMachine extends StateMachine<Character> {

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
    public void onTransition(Character edge) {
        this.buffer.append(edge);
    }

    /**
     * @return content of the buffer
     */
    public String getBufferContent() {
        return this.buffer.toString();
    }
}
