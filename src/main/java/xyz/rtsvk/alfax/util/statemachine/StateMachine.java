package xyz.rtsvk.alfax.util.statemachine;

import java.util.*;
import java.util.function.Supplier;


/**
 * Class representing a state machine
 * @author Jastrobaron
 * @param <E> transition edge type
 * @param <P> state machine product type
 */
public abstract class StateMachine<E, P> {

	/** Error state */
	private final State<E> ERROR = new State<>("#ERR_STATE", false);
	/** Set of states */
	private final Set<State<E>> states;
	/** Buffer to temporarily store symbols that caused the state machine to fall into {@link #ERROR} state */
	private final Queue<E> pushbackBuffer;
	/** Current state of the state machine */
	private State<E> cstate;
	/** Initial state of the state machine */
	private State<E> start;
	/** Supplier of input symbols */
	private Supplier<E> inputSupplier;

	/**
	 * Class constructor
	 */
	public StateMachine() {
		this.states = new HashSet<>();
		this.pushbackBuffer = new LinkedList<>();
		this.inputSupplier = null;
	}

	/**
	 * Transition to a new state
	 * @return {@code true} if the transition attempt lead to the error state, {@code false} otherwise
	 */
	public TransitionResult transition() {
		E edge = getInput();
 		State<E> nstate = this.cstate.getTransition(edge, ERROR);
		if (nstate.equals(ERROR)) {
			this.pushbackBuffer.offer(edge);
			if (this.cstate.isFinite()) {
				return TransitionResult.SUCCESS;
			} else {
				return TransitionResult.FAILED;
			}
		}
		this.cstate = nstate;
		this.onTransition(edge);
		return TransitionResult.NEXT_SYMBOL;
	}

	/**
	 * Run the analysis once
	 * @return {@code true} if the analysis was successful, {@code false} otherwise
	 */
	public boolean runOnce() {
		this.reset();
		TransitionResult tickResult;
		do {
			tickResult = transition();
		} while (tickResult == TransitionResult.NEXT_SYMBOL);
		return tickResult == TransitionResult.SUCCESS;
	}

	/**
	 * @return symbol from the input buffer
	 */
	private E getInput() {
		if (this.inputSupplier == null) {
			throw new IllegalStateException("Input supplier is not set!");
		} else if (this.pushbackBuffer.isEmpty()) {
			return this.inputSupplier.get();
		} else {
			return this.pushbackBuffer.poll();
		}
	}

	/**
	 * @return the next product
	 */
	public P getNext() {
		boolean success = runOnce();
		if (success) {
			return this.getResult();
		} else {
			return null;
		}
	}

	/**
	 * Processes the entire input into a list of products
	 * @return list of products
	 */
	public List<P> getAll() {
		List<P> result = new ArrayList<>();
		P product;
		while ((product = this.getNext()) != null) {
			result.add(product);
		}
		return result;
	}

	/**
	 * Create a new state within the state machine
	 * @param name of the state
	 * @param finite whether the state is to be interpreted as finite
	 * @return the created state object
	 */
	public State<E> createState(String name, boolean finite) {
		State<E> state = new State<>(name, finite);
		this.addStates(state);
		return state;
	}

	/**
	 * Add new states to the state machine
	 * @param states to add
	 */
	@SafeVarargs
	public final void addStates(State<E>... states) {
		this.states.addAll(Arrays.asList(states));
	}

	/**
	 * Set the initial state of the state machine
	 * @param state to set as the initial state
	 */
	public void setInitialState(State<E> state) {
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
	public State<E> getCurrentState() {
		return this.cstate;
	}

	/**
	 * Set the input symbol supplier
	 * @param inputSupplier function that supplies the input symbols
	 */
	public void setInputSupplier(Supplier<E> inputSupplier) {
		this.inputSupplier = inputSupplier;
	}

	/**
	 * Function to call when a successful transition occurs
	 * @param edge transition that occurred
	 */
	protected abstract void onTransition(E edge);

	/**
	 * @return result of the state machine analysis
	 */
	protected abstract P getResult();

}
