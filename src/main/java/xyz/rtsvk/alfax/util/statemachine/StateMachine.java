package xyz.rtsvk.alfax.util.statemachine;

import java.util.*;

/**
 * Class representing a state machine
 * @author Jastrobaron
 * @param <T> transition edge type
 */
public abstract class StateMachine<T> {

	/** Error state */
	private final State<T> ERROR = new State<>("#ERR_STATE", false);
	/** Set of states */
	private final Set<State<T>> states;
	/** Current state of the state machine */
	private State<T> cstate;
	/** Initial state of the state machine */
	private State<T> start;

	/**
	 * Class constructor
	 */
	public StateMachine() {
		this.states = new HashSet<>();
	}

	/**
	 * Transition to a new state
	 * @param edge to use to transition
	 * @return {@code true} if the transition attempt lead to the error state, {@code false} otherwise
	 */
	public boolean transition(T edge) {
		State<T> nstate = this.cstate.getTransition(edge, ERROR);
		if (this.cstate.isFinite() || nstate.equals(ERROR)) {
			return true;
		}
		this.cstate = nstate;
		this.onTransition(edge);
		return false;
	}

	/**
	 * Add new states to the state machine
	 * @param states to add
	 */
	@SafeVarargs
	public final void addStates(State<T>... states) {
		this.states.addAll(Arrays.asList(states));
	}

	/**
	 * Set the initial state of the state machine
	 * @param state to set as the initial state
	 */
	public void setInitialState(State<T> state) {
		this.start = state;
	}

	/**
	 * Resets the state machine to its initial state
	 */
	public void reset() {
		this.cstate = this.start;
	}

	/**
	 * @return current state of the state machine
	 */
	public State<T> getCurrentState() {
		return this.cstate;
	}

	/**
	 * Function to call when a successful transition occurs
	 * @param edge transition that occurred
	 */
	public abstract void onTransition(T edge);
}
