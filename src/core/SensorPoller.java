package core;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A class that polls all sensors at once.
 * Calls sensors' poll() method periodically.
 *
 * @author Andrew Duffy <gerberduffy@gmail.com>
 */
public class SensorPoller extends GRTLoggedProcess{

    private Vector sensors;
    
    private final static int DEFAULT_POLLTIME = 10;

    /**
     * Creates a new SensorPoller with no sensors, polling every {@value 
     * #DEFAULT_POLLTIME} ms.
     */
    public SensorPoller() {
        this(new Vector());
    }
    
    /**
     * Creates a new SensorPoller with no sensors.
     * 
     * @param pollTime how often to poll, in ms
     */
    public SensorPoller(int pollTime) {
        this(new Vector(), pollTime);
    }
    
    /**
     * Creates a new SensorPoller, polling every {@value #DEFAULT_POLLTIME} ms.
     * 
     * @param sensors vector of sensors to poll
     */
    public SensorPoller(Vector sensors) {
        this(sensors, DEFAULT_POLLTIME);
    }

    /**
     * Creates a new SensorPoller.
     * 
     * @param sensors vector of sensors to poll
     * @param pollTime how often to poll, in ms
     */
    public SensorPoller(Vector sensors, int pollTime) {
        super("Sensor poller", pollTime);
        this.sensors = sensors;
    }

    /**
     * Adds a sensor to the vector of sensors.
     * 
     * @param s sensor to poll 
     */
    public void addSensor(Sensor s) {
        sensors.addElement(s);
    }
    
    /**
     * Removes a sensor from the vector of sensors.
     * 
     * @param s sensor to not poll
     */
    public void removeSensor(Sensor s) {
        sensors.removeElement(s);
    }
    
    protected void poll() {
        for (Enumeration en = sensors.elements(); en.hasMoreElements();) {
            ((Sensor) en.nextElement()).poll();
        }
    }
}
