package core;

/**
 * An EventController describes behavior based on received events.
 *
 * @author ajc
 */
public abstract class EventController extends GRTLoggedProcess {
    
    protected boolean enabled = false;

    public EventController(String name) {
        super(name);
    }

    /**
     * Starts listening to events.
     */
    protected abstract void startListening();

    /**
     * Stops listening to events.
     */
    protected abstract void stopListening();

    /**
     * Enables actions, and begins listening.
     */
    public void enable() {
        //enable() always works because an EventController is always running
        enabled = true;
        startListening();
    }

    /**
     * Disables actions, and stops listening.
     */
    public void disable() {
        enabled = false;
        stopListening();
    }
}
