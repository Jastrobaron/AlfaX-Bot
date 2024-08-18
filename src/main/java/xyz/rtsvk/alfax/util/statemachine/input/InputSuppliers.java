package xyz.rtsvk.alfax.util.statemachine.input;

import xyz.rtsvk.alfax.util.statemachine.lex.StringBufferStateMachine;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public class InputSuppliers {

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

    public static Supplier<String> fromStateMachine(StringBufferStateMachine stateMachine) {
        return stateMachine::getNext;
    }

}
