package xyz.rtsvk.alfax.util.statemachine.parsers;

import xyz.rtsvk.alfax.util.statemachine.Predicates;
import xyz.rtsvk.alfax.util.statemachine.State;
import xyz.rtsvk.alfax.util.statemachine.StateMachine;
import xyz.rtsvk.alfax.util.statemachine.lex.KeyValueStateMachine;
import xyz.rtsvk.alfax.util.tuples.Pair;

/**
 * State machine for parsing the output of {@link KeyValueStateMachine}
 * @author Jastrobaron
 */
public class KVParserStateMachine extends StateMachine<String, Pair<String, String>> {

    /** Equal sign ('=') character */
    private static final String ASSIGN = KeyValueStateMachine.KEY_VALUE_DELIMITER.toString();

    /** Initial state */
    private final State<String> start;
    /** Key reading state */
    private final State<String> key;
    /** Value reading state */
    private final State<String> value;

    /** Result of the analysis */
    private Pair<String, String> result;

    public KVParserStateMachine(String pairDelimiter) {
        this.result = new Pair<>();
        this.start = createState("start", false);
        this.key = createState("key", false);
        this.value = createState("value", true);
        State<String> assign = createState("equal", false);

        this.start.addTransition(Predicates.isValidIdentifier(), this.key);
        this.key.addTransition(Predicates.stringEquals(ASSIGN), assign);
        this.value.addTransition(Predicates.stringEquals(pairDelimiter), this.start);
        assign.addTransition(Predicates.anyStringExcept(), this.value);

        this.setInitialState(this.start);
    }

    @Override
    protected void onTransition(String edge) {
        if (getCurrentState().equals(this.start)) {
            this.result = new Pair<>();
        } else if (getCurrentState().equals(this.key)) {
            this.result.setKey(edge);
        } else if (getCurrentState().equals(this.value)) {
            this.result.setValue(edge);
        }
    }

    @Override
    protected Pair<String, String> getResult() {
        return this.result;
    }
}
