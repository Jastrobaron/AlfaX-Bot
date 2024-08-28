package xyz.rtsvk.alfax.services;

/**
 * Exception to be thrown when a service fails to start
 * @author Jastrobaron
 */
public class ServiceStartupFailedException extends RuntimeException {
    /**
     * Constructor
     * @param cause of the startup failure
     */
    public ServiceStartupFailedException(Throwable cause) {
        super(cause);
    }
}
