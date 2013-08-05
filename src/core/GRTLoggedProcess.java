package core;

import logger.GRTLogger;

/**
 * A GRTLoggedProcess is a controllable process. It can be initiated/terminated.
 * When a GRTLoggedProcess is constructed, it is not run.
 *
 * Logging is done to the static GRTLogger.
 *
 * @author ajc
 */
public abstract class GRTLoggedProcess {

    protected final String name;
    protected boolean running = false;
    private int sleepTime;
    private Thread thread = null;

    /**
     * Constructs a new GRTLoggedProcess that does not poll.
     *
     * @param name name of process.
     */
    public GRTLoggedProcess(String name) {
        this(name, -1);
    }

    /**
     * Constructs a new GRTLoggedProcess that polls.
     *
     * @param name name of process.
     * @param sleepTime time to pause for between executions of poll(), in
     * milliseconds.
     */
    public GRTLoggedProcess(String name, int sleepTime) {
        this.name = name;
        this.sleepTime = sleepTime;
    }

    private Runnable poller = new Runnable() {
        public void run() {
            running = true;
            while (running && sleepTime >= 0) {
                //only poll, and thus only send events, if enabled
                poll();

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            thread = null;
        }
    };

    /**
     * Starts polling.
     */
    public void startPolling() {
        if (sleepTime >= 0 && !isRunning()) {
            thread = new Thread(poller);
            thread.start();
        }
    }

    /**
     * In order to poll and have meaningful effects, poll() must be overridden.
     */
    protected void poll() {
    }

    /**
     * Logs a message.
     *
     * @param message message to logInfo.
     */
    protected void logInfo(String message) {
        GRTLogger.logInfo(toString() + "\t" + message);
    }

    /**
     * Logs an error message.
     *
     * @param message message to logInfo.
     */
    protected void logError(String message) {
        GRTLogger.logError(toString() + "\t" + message);
    }

    /**
     * Logs a success message.
     *
     * @param message message to logInfo.
     */
    protected void logSuccess(String message) {
        GRTLogger.logSuccess(toString() + "\t" + message);
    }

    /**
     * Stops execution of this process.
     */
    public void halt() {
        running = false;
    }

    /**
     * Returns whether or not this process is running.
     *
     * @return true if running, false otherwise.
     */
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    /**
     * Returns the name of this process.
     *
     * @return name of this process.
     */
    public String getID() {
        return name;
    }

    /*
     * toString method, returns loggable string in the format
     * [[ClassName:Id]].
     * 
     * @return loggable string.
     */
    public String toString() {
        return "[[" + getID() + "]]";
    }

    /**
     * Sets how long to sleep for.
     *
     * @param millis time to sleep for between polls, in milliseconds
     */
    protected void setSleepTime(int millis) {
        sleepTime = millis;
    }
}
