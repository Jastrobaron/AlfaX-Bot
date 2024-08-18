package xyz.rtsvk.alfax.util.statemachine.lex;

import xyz.rtsvk.alfax.util.statemachine.Predicates;
import xyz.rtsvk.alfax.util.statemachine.State;

import java.util.function.Predicate;

/**
 * Class representing a state machine that is used in parsing KV data
 * @author Jastrobaron
 */
public class KeyValueStateMachine extends StringBufferStateMachine {

	/** Character to separate the key from the value in a key-value pair */
	public static Character KEY_VALUE_DELIMITER = '=';

	/**
	 * Class constructor
	 * @param separator of key-value entries
	 */
	public KeyValueStateMachine(Character separator) {
		State<Character> start = createState("start", false);
		State<Character> key = createState("key", false);
		State<Character> equalSign = createState("equalSign", false);
		State<Character> value = createState("value", true);
		State<Character> delimiter = createState("delimiter", true);

		start.addTransition(Predicates.charEquals(KEY_VALUE_DELIMITER), key);
		key.addTransition(Predicates.charEquals(KEY_VALUE_DELIMITER), key);
		key.addTransition(Predicates.anyCharExcept(KEY_VALUE_DELIMITER), equalSign);

		equalSign.addTransition(Predicates.charEquals(separator), value);
		value.addTransition(Predicates.charEquals(separator), value);
		value.addTransition(Predicates.anyCharExcept(separator), delimiter);

		this.setInitialState(start);
	}
}
