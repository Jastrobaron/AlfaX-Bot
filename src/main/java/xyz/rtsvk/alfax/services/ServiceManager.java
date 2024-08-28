package xyz.rtsvk.alfax.services;

import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.ServiceTaskScheduler;
import xyz.rtsvk.alfax.util.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class responsible for managing services and restarting them on crash
 * @author Jastrobaron
 */
public class ServiceManager extends Thread implements Thread.UncaughtExceptionHandler {

	/** Logger for this class */
	private static final Logger logger = new Logger(ServiceManager.class);

	/** Map of callbacks to call when a thread crashes */
	private final Map<String, Supplier<Thread>> services;
	/** List of instances that are currently running */
	private final Map<String, Thread> serviceInstances;
	/** Task scheduler */
	private final ServiceTaskScheduler scheduler;
	/** Flag to indicate whether the watcher is running */
	private boolean running;

	/**
	 * Class constructor
	 */
	public ServiceManager() {
		this.services = new HashMap<>();
		this.serviceInstances = new HashMap<>();
		this.scheduler = new ServiceTaskScheduler();
	}

	@Override
	public void run() {
		this.serviceInstances.values().forEach(Thread::start);
		while (this.running) {
			this.scheduler.runJobs();
		}
		this.serviceInstances.values().forEach(Thread::interrupt);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (e instanceof InterruptedException) {
			return;
		} else if (e instanceof ServiceStartupFailedException) {
			logger.error(TextUtils.format("Service '${0}' failed to start: ${1}", t.getName(), e.getMessage()));
			e.printStackTrace();
			return;
		}

		String name = t.getName();
		logger.error("Thread '" + name + "' crashed!");
		System.err.println(e.getMessage());
		logger.info("Restarting...");

		if (this.serviceInstances.remove(name) == null) {
			logger.error(TextUtils.format("Unable to remove service instance with name '${0}', as it does not exist!", name));
			return;
		}

		Supplier<Thread> serviceSupplier = this.services.get(name);
		if (serviceSupplier == null) {
			logger.error(TextUtils.format("Service with name '${0}' was not found!", name));
			return;
		}

		Thread instance = serviceSupplier.get();
		instance.setUncaughtExceptionHandler(this);
		this.serviceInstances.put(instance.getName(), instance);
		instance.start();
	}

	@Override
	public void start() {
		this.running = true;
		super.start();
	}

	@Override
	public void interrupt() {
		this.scheduleTask(() -> this.running = false);
		super.interrupt();
	}

	/**
	 * Add a new service to the manager
	 * @param supplier of the service instances
	 */
	public void addService(Supplier<Thread> supplier) {
		Thread service = supplier.get();
		String srvName = service.getName();
		if (this.services.containsKey(srvName)) {
			logger.warn(TextUtils.format("Service supplier with the name '${0}' already exists!", srvName));
			return;
		}
		this.services.putIfAbsent(srvName, supplier);
		if (!isServiceInstantiated(srvName)) {
			service.setUncaughtExceptionHandler(this);
			this.serviceInstances.put(srvName, service);
		}
	}

	/**
	 * Schedule a new task
	 * @param task to run
	 */
	public void scheduleTask(Runnable task) {
		this.scheduler.schedule(task);
	}

	/**
	 * Check if a service with the specified name was already instantiated
	 * @param name of the service
	 * @return {@code true} if the service was instantiated, {@code false} otherwise
	 */
	private boolean isServiceInstantiated(String name) {
		return this.serviceInstances.containsKey(name);
	}
}
