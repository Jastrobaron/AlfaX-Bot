package xyz.rtsvk.alfax.util.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Class representing a state within the state machine
 * @author Jastrobaron
 * @param <T> type of the edge
 */
public class State<T> {

	/** Map of transitions to other states */
	private final Map<Predicate<T>, State<T>> transitions;
	/** Name of the state */
	private final String name;
	/** Flag specifying whether the state is finite */
	private final boolean finite;

	/**
	 * Class constructor
	 * @param name of the state
	 * @param finite whether the state is finite
	 */
	public State(String name, boolean finite) {
		this.transitions = new HashMap<>();
		this.name = name;
		this.finite = finite;
	}

	/**
	 * Adds a new outbound transition
	 * @param edge used to transition
	 * @param state to transition to
	 */
	public void addTransition(T edge, State<T> state) {
		this.transitions.put(e -> e.equals(edge), state);
	}

	/**
	 * Adds a new outbound predicate transition
	 * @param predicate used to transition
	 * @param state to transition to
	 */
	public void addTransition(Predicate<T> predicate, State<T> state) {
		this.transitions.put(predicate, state);
	}

	/**
	 * Gets the new state to transition to
	 * @param edge used to transition
	 * @param fallback to fall back to when an invalid edge is supplied (to be used with {@link StateMachine#ERROR})
	 * @return the new state
	 */
	public State<T> getTransition(T edge, State<T> fallback) {
		return this.transitions.entrySet()
				.stream()
				.filter(e -> e.getKey().test(edge))
				.map(Map.Entry::getValue)
				.findFirst().orElse(fallback);
	}

	/**
	 * @return name of the state
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return {@code true} if the state is finite, {@code false} otherwise
	 */
	public boolean isFinite() {
		return this.finite;
	}
}
