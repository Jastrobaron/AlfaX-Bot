package xyz.rtsvk.alfax.util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * FIFO task scheduler
 * @author Jastrobaron
 */
public class ServiceTaskScheduler {

    /** Job queue */
    private final Queue<Runnable> jobs;
    /** Synchronization lock for the job queue */
    private final Object jobQueueLock;

    /**
     * Class constructor
     */
    public ServiceTaskScheduler() {
        this.jobs = new LinkedList<>();
        this.jobQueueLock = new Object();
    }

    /**
     * Run all the scheduled jobs. To be called from the service thread.
     */
    public void runJobs() {
        synchronized (this.jobQueueLock) {
            while (!this.jobs.isEmpty()) {
                try {
                    this.jobs.poll().run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Schedule a new job
     * @param job to run
     */
    public void schedule(Runnable job) {
        synchronized (this.jobQueueLock) {
            this.jobs.offer(job);
        }
    }
}
