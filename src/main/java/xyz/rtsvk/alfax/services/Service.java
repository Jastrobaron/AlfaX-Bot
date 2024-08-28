package xyz.rtsvk.alfax.services;

/**
 * Base class for implementing services
 * @author Jastrobaron
 */
public abstract class Service extends Thread {

    /** Flag to indicate whether the service is running */
    private boolean running;

    /**
     * Constructor
     * @param name of the service
     */
    public Service(String name) {
        super(name);
        this.setRunning(false);
    }

    @Override
    public void run() {
        try {
            this.startup();
        } catch (Exception e) {
            throw new ServiceStartupFailedException(e);
        }
        this.setRunning(true);
        try {
            while (isRunning()) {
                this.loop();
            }
            this.shutdown();
        } catch (Exception e) {
            this.setRunning(false);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void interrupt() {
        this.setRunning(false);
        super.interrupt();
    }

    /**
     * @return {@code true} if the service is running, {@code false} otherwise
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Set the new value of the {@link #running} flag
     * @param running the new value
     */
    protected void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Method to be called when the service starts up.
     * @throws Exception when an error occurs. A {@link ServiceStartupFailedException} is then thrown within the thread
     */
    protected abstract void startup() throws Exception;

    /**
     * Main loop of the service. Called infinitely while the {@link #running} flag is {@code true}
     * @throws Exception when an error occurs. A {@link RuntimeException} is then thrown within the thread.
     */
    protected abstract void loop() throws Exception;

    /**
     * Method to be called when a service is shutting down peacefully.
     * @throws Exception when an error occurs. A {@link RuntimeException} is then thrown within the thread.
     */
    protected abstract void shutdown() throws Exception;
}
