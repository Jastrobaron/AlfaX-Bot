package xyz.rtsvk.alfax.util.statemachine;

/**
 * Class representing the initial state of the state machine
 * @param <T> edge type
 * @author Jastrobaron
 */
public class InitialState<T> extends State<T> {
	/**
	 * Class constructor
	 */
	public InitialState() {
		super("Start", false);
	}
}
