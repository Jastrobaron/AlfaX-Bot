package xyz.rtsvk.alfax.commands.sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandSet {
	private final String name;
	private final Map<String, Runnable> commands;
	private final List<Thread> executors;

	/**
	 * Creates a new command set with the given name
	 * @param name the name of the set
	 */
	public CommandSet(String name) {
		this.name = name;
		this.commands = new HashMap<>();
		this.executors = new ArrayList<>();
	}

	/**
	 * Adds a command to the set
	 * @param name the name of the command
	 * @param executor command executor
	 */
	protected void addCommand(String name, Runnable executor) {
		this.commands.put(name, executor);
	}

	/**
	 * Returns the executor for the command with the given name
	 * @param name the name of the command
	 * @return the executor
	 */
	public Runnable getExecutor(String name) {
		return this.commands.get(name);
	}

	/**
	 * Executes a command with the given name
	 * @param name the name of the command
	 */
	public void executeCommand(String name) {
		Thread executor = new Thread(getExecutor(name));
		executor.start();
		this.executors.add(executor);
	}
}
