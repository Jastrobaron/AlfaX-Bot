package xyz.rtsvk.alfax.util.statemachine.impl;

import xyz.rtsvk.alfax.util.statemachine.State;
import xyz.rtsvk.alfax.util.statemachine.Predicates;

import java.util.List;

/**
 * State machine to use for separated value formats (e.g. CSV)
 * @author Jastrobaron
 */
public class SeparatedValuesStateMachine extends StringBufferStateMachine {

    /** Start of an escape sequence */
    private static final Character ESCAPE_CHAR = '\\';

    /**
     * Constructor
     * @param separatorChar character to use as the entry separator
     * @param quotes list of characters that can be used as a separator
     */
    public SeparatedValuesStateMachine(Character separatorChar, List<Character> quotes) {
        State<Character> start = new State<>("start", false);
        State<Character> separator = new State<>("separator", true);
        State<Character> entry = new State<>("entry", true);

        // quoted strings
        for (Character quoteMk : quotes) {
            State<Character> stringStart = new State<>("string_start_" + quoteMk, false);
            State<Character> string = new State<>("string_" + quoteMk, false);
            State<Character> stringEscape = new State<>("string_escape_" + quoteMk, false);
            State<Character> stringEnd = new State<>("string_end_" + quoteMk, true);

            start.addTransition(Predicates.charEquals(quoteMk), stringStart);
            stringStart.addTransition(Predicates.charEquals(quoteMk), stringEnd);
            stringStart.addTransition(Predicates.anyCharExcept(quoteMk), string);

            string.addTransition(Predicates.anyCharExcept(quoteMk), string);
            string.addTransition(Predicates.charEquals(ESCAPE_CHAR), stringEscape);
            string.addTransition(Predicates.charEquals(quoteMk), stringEnd);
            stringEscape.addTransition(Predicates.anyCharExcept(), string);

            this.addStates(stringStart, stringEnd, string, stringEscape);
        }

        start.addTransition(Predicates.charEquals(separatorChar), separator);
        start.addTransition(Predicates.anyCharExcept(quotes), entry);
        entry.addTransition(Predicates.anyCharExcept(quotes), entry);

        this.setInitialState(start);
        this.addStates(start, separator, entry);
    }
}
