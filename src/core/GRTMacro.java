/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import event.events.MacroEvent;
import event.listeners.MacroListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A GRTMacro specifies code to complete one discrete motion (ie. Turn a
 * specified angle, drive a specific distance). Most useful for autonomous mode.
 *
 * @author andrew, keshav
 */
public abstract class GRTMacro extends GRTLoggedProcess {

    private boolean hasCompletedExecution = false;
    private boolean hasTimedOut = false;
    private boolean hasInitialized = false;
    private boolean hasStarted = false;
    private boolean alive = false;
    private Vector macroListeners;
    private int timeout;
    private long startTime;
    private int pollTime;
    private static final int NOTIFY_INITIALIZE = 0;
    private static final int NOTIFY_COMPLETED = 1;
    private static final int NOTIFY_TIMEDOUT = 2;

    /**
     * Creates a new macro.
     *
     * @param name Name of macro
     * @param timeout Time in ms after which macro will automatically stop
     * execution
     * @param pollTime Time in ms how often to call perform()
     */
    public GRTMacro(String name, int timeout, int pollTime) {
        super(name);
        this.timeout = timeout;
        this.pollTime = pollTime;
        macroListeners = new Vector();
    }

    /**
     * A GRTMacro specifies code to complete one discrete motion (ie. Turn a
     * specified angle, drive a specific distance). Most useful for autonomous
     * mode
     *
     * @param name Name of macro
     * @param timeout Time in ms after which macro will automatically stop
     * execution
     */
    public GRTMacro(String name, int timeout) {
        this(name, timeout, 50);
    }

    /**
     * Executes the macro. If it has not been started, it initializes the macro
     * and repeatedly calls perform() until the macro has completed execution,
     * then calls die().
     */
    public void execute() {
        if (!hasStarted) {
            hasStarted = true;

            logInfo("Initializing Macro...");
            initialize();
            hasInitialized = true;
            alive = true;
            notifyListeners(NOTIFY_INITIALIZE);
            this.startTime = System.currentTimeMillis();

            while (!hasCompletedExecution) {
                perform();

                try {
                    Thread.sleep(pollTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                
                if ((System.currentTimeMillis() - startTime) > timeout) {
                    hasCompletedExecution = true;
                    hasTimedOut = true;
                    notifyListeners(NOTIFY_TIMEDOUT);
                }
            }
            
            kill();
            
            System.out.println("Notify that we died.");
            notifyListeners(NOTIFY_COMPLETED);
        }
    }

    /**
     *
     * @return state of macro initialization
     */
    public boolean isInitialized() {
        return hasInitialized;
    }

    /**
     *
     * @return whether or not the macro has completed execution.
     */
    public boolean isDone() {
        return hasCompletedExecution;
    }

    /**
     *
     * @return Whether or not the macro has timed out.
     */
    public boolean isTimedOut() {
        return hasTimedOut;
    }

    /**
     * Resets the macro, as if it had never began.
     */
    public void reset() {
        hasCompletedExecution = hasStarted = hasTimedOut = alive = false;
    }
    
    public boolean isAlive(){
        return alive;
    }

    /**
     * Macro initialization.
     */
    protected abstract void initialize();

    /**
     * Implemented on a per-macro basis
     */
    protected abstract void perform();

    protected abstract void die();
    
    /**
     * Call this to indicate that the loop should no longer run.
     */
    protected final void notifyFinished() {
        hasCompletedExecution = true;
    }

    /**
     * After executing, or to forcibly halt a macro
     */
    public void kill() {
        if (isAlive()) {
            System.out.println("Killing macro: " + name);
            hasCompletedExecution = true;
            alive = false;
            die();
        }
    }
    
    public void addListener(MacroListener l) {
        macroListeners.addElement(l);
    }

    public void removeListener(MacroListener l) {
        macroListeners.removeElement(l);
    }

    private void notifyListeners(int id) {
        switch (id) {
            case NOTIFY_COMPLETED:
                for (Enumeration en = macroListeners.elements(); en.
                        hasMoreElements();) {
                    ((MacroListener) en.nextElement()).macroDone(new MacroEvent(this));
                }
                break;
            case NOTIFY_TIMEDOUT:
                for (Enumeration en = macroListeners.elements(); en.
                        hasMoreElements();) {
                    ((MacroListener) en.nextElement()).macroTimedOut(new MacroEvent(this));
                }
                break;
            case NOTIFY_INITIALIZE:
                for (Enumeration en = macroListeners.elements(); en.
                        hasMoreElements();) {
                    ((MacroListener) en.nextElement()).macroInitialized(new MacroEvent(this));
                }
                break;
        }

    }
}
