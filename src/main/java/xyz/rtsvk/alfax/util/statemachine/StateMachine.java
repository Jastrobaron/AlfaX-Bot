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
	private final State<E> ERROR = new ErrorState<>();
	/** Initial state of the state machine */
	private final State<E> START = new InitialState<>();;
	/** Current state of the state machine */
	private State<E> cstate;
	/** Supplier of input symbols */
	private Supplier<E> inputSupplier;
	/** Pushback edge */
	private E pushbackBuffer;
	/** Flag to indicate whether the end of input stream was reached */
	private boolean endReached;

	/**
	 * Class constructor
	 */
	public StateMachine() {
		this.pushbackBuffer = null;
		this.inputSupplier = null;
		this.endReached = false;
	}

	/**
	 * Transition to a new state
	 * @return {@code true} if the transition attempt lead to the error state, {@code false} otherwise
	 */
	public TransitionResult transition() {
		E edge = readInput();
		if (edge == null) {
			this.endReached = true;
			return TransitionResult.SUCCESS;
		}
 		State<E> nstate = this.cstate.getTransition(edge, ERROR);
		if (nstate instanceof ErrorState<E>) {
			this.pushbackBuffer = edge;
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
	 * @return symbol from the input buffer, primarily from the pushback buffer
	 */
	private E readInput() {
		if (this.inputSupplier == null) {
			throw new IllegalStateException("Input supplier is not set!");
		} else if (this.pushbackBuffer == null) {
			return this.inputSupplier.get();
		} else {
			E bufferVal = this.pushbackBuffer;
			this.pushbackBuffer = null;
			return bufferVal;
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
	 * Get the initial state of the state machine
	 * @return the initial state object
	 */
	public State<E> getInitialState() {
		return this.START;
	}

	/**
	 * Resets the state machine to its initial state
	 */
	public void reset() {
		this.cstate = this.START;
		this.endReached = false;
	}

	/**
	 * @return current state of the state machine
	 */
	protected final State<E> getCurrentState() {
		return this.cstate;
	}

	/**
	 * Set the input symbol supplier
	 * @param inputSupplier function that supplies the input symbols
	 */
	public void setInput(Supplier<E> inputSupplier) {
		this.inputSupplier = inputSupplier;
	}

	/**
	 * Predicate to check whether the end of input stream was reached
	 * @return {@code true} if the end of input stream was reached, {@code false} otherwise
	 */
	public boolean isEndReached() {
		return this.endReached;
	}

	/**
	 * Function to call when a successful transition occurs
	 * @param edge transition that occurred
	 */
	protected abstract void onTransition(E edge);

	/**
	 * Returns the product
	 * @return result of the state machine analysis
	 */
	protected abstract P getResult();
}
