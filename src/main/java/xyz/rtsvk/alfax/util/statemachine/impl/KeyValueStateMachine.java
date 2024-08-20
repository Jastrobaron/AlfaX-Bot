package xyz.rtsvk.alfax.util.statemachine.impl;

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
		State<Character> start = new State<>("start", false);
		State<Character> key = new State<>("key", false);
		State<Character> equalSign = new State<>("equalSign", false);
		State<Character> value = new State<>("value", true);
		State<Character> delimiter = new State<>("delimiter", true);

		Predicate<Character> keyMatcher = KEY_VALUE_DELIMITER::equals;
		start.addTransition(keyMatcher, key);
		key.addTransition(keyMatcher, key);
		key.addTransition(keyMatcher.negate(), equalSign);

		Predicate<Character> valueMatcher = e -> e.equals(separator);
		equalSign.addTransition(valueMatcher, value);
		value.addTransition(valueMatcher, value);
		value.addTransition(valueMatcher.negate(), delimiter);

		this.setInitialState(start);
		this.addStates(start, key, equalSign, value, delimiter);
	}
}
