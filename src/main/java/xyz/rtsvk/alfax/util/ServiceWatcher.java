package xyz.rtsvk.alfax.util;

public class ServiceWatcher implements Thread.UncaughtExceptionHandler {

	private final Logger logger;

	public ServiceWatcher() {
		this.logger = new Logger(this.getClass());
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		if (e.getCause() instanceof InterruptedException) return;
		this.logger.error("Thread '" + t.getName() + "' crashed! " + e.getMessage());
		this.logger.info("Restarting...");
		t.start();
	}
}
