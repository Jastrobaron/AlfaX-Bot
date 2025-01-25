package xyz.rtsvk.alfax.util.parsing.statemachine;

/**
 * Class to unambiguously represent the error state within the state machine
 *
 * @see State
 * @param <E> edge type
 * @author Jastrobaron
 */
public class ErrorState<E> extends State<E> {
	/**
	 * Class constructor
	 */
	public ErrorState() {
		super("#ERR_STATE", false);
	}
}
