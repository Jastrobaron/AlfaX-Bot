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
	/** Current state of the state machine */
	private State<E> cstate;
	/** Initial state of the state machine */
	private State<E> start;
	/** Supplier of input symbols */
	private Supplier<E> inputSupplier;
	/** Pushback edge */
	private E pushbackBuffer;

	/**
	 * Class constructor
	 */
	public StateMachine() {
		this.start = new InitialState<>();
		this.pushbackBuffer = null;
		this.inputSupplier = null;
	}

	/**
	 * Transition to a new state
	 * @return {@code true} if the transition attempt lead to the error state, {@code false} otherwise
	 */
	public TransitionResult transition() {
		E edge = getInput();
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
	 * @return symbol from the input buffer
	 */
	private E getInput() {
		if (this.inputSupplier == null) {
			throw new IllegalStateException("Input supplier is not set!");
		} else if (this.pushbackBuffer == null) {
			return this.inputSupplier.get();
		} else {
			E retval = this.pushbackBuffer;
			this.pushbackBuffer = null;
			return retval;
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
		return this.start;
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
