package xyz.rtsvk.alfax.util.statemachine;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Factory class for creating {@link Supplier <T>} instances that are then used
 * to provide input for {@link StateMachine}
 * @author Jastrobaron
 */
public class Inputs {

    /**
     * Creates an input supplier from the supplied string
     * @param input string to create the supplier from
     * @return the created supplier
     */
    public static Supplier<Character> fromString(String input) {
        return new Supplier<>() {
            private int index = 0;
            @Override
            public Character get() {
                if (this.index < input.length()) {
                    return input.charAt(index++);
                } else {
                    return null;
                }
            }
        };
    }

    /**
     * Creates an input supplier from the supplied {@link InputStream}
     * @param in stream to read
     * @return the created supplier
     */
    public static Supplier<Character> fromInputStream(InputStream in) {
        return () -> {
            try {
                int readVal = in.read();
                return readVal != -1 ? (char) readVal : null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Creates an input supplier from another {@link StateMachine}
     * @param stateMachine to read from
     * @return the created supplier
     */
    public static Supplier<String> fromStateMachine(GenericLexicalAnalyzer stateMachine) {
        return stateMachine::getNext;
    }

}
