package xyz.rtsvk.alfax.util.parsing.statemachine;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
	 * Adds a new self transition
	 * @param edge used to transition
	 */
	public void addTransition(T edge) {
		this.addTransition(edge, this);
	}

	/**
	 * Adds a new self predicate transition
	 * @param predicate used to transition
	 */
	public void addTransition(Predicate<T> predicate) {
		this.addTransition(predicate, this);
	}

	/**
	 * Creates a new state and add an outbound transition to it
	 * @param edge predicate to make the transition
	 * @param name of the new state
	 * @param finite whether the state is finite
	 * @return the created state
	 */
	public State<T> createOutboundState(T edge, String name, boolean finite) {
		return this.createOutboundState(Predicates.equal(edge), name, finite);
	}

	/**
	 * Creates a new state and add an outbound transition to it
	 * @param edge to make the transition
	 * @param name of the new state
	 * @param finite whether the state is finite
	 * @return the created state
	 */
	public State<T> createOutboundState(Predicate<T> edge, String name, boolean finite) {
		State<T> state = new State<>(name, finite);
		this.addTransition(edge, state);
		return state;
	}

	/**
	 * Gets the new state to transition to
	 * @param edge used to transition
	 * @param fallback to fall back to when an invalid edge is supplied (to be used with {@link StateMachine#ERROR})
	 * @return the new state
	 */
	public State<T> getTransition(T edge, State<T> fallback) {
		if (Objects.isNull(edge)) {
			return fallback;
		}

		for (Map.Entry<Predicate<T>, State<T>> entry : this.transitions.entrySet()) {
			if (entry.getKey().test(edge)) {
				return entry.getValue();
			}
		}
		return fallback;
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
