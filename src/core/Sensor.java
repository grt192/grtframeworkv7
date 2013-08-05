package core;

/**
 * A sensor sends numeric sensor event data. They only send data when running.
 * Sensors can either receive data through events, or by polling. It
 * additionally stores the state of variables and performs state change checks.
 * 
 * For performance reasons, sensors do not poll on their own. Instead, sensors
 * may be fed to a SensorPoller to have the SensorPoller poll the sensors
 * instead.
 *
 * @author ajc
 */
public abstract class Sensor extends GRTLoggedProcess {

    //Constants
    public static final double TRUE = 1.0;
    public static final double FALSE = 0.0;
    public static final double ERROR = Double.NaN;
    //Instance variables
    private double[] data;

    /**
     * Construct a sensor.
     *
     * @param name name of the sensor.
     * @param sleepTime time between polls [ms].
     * @param numData number of pieces of data.
     */
    public Sensor(String name, int numData) {
        super(name);
        logInfo("New non-threaded sensor as well!");
        running = true;
        data = new double[numData];
    }

    /**
     * Stores a datum, and notifies listeners if the state of it has changed.
     *
     * @param id key of the data
     * @param datum fresh datum
     */
    protected void setState(int id, double datum) {
        double previous = data[id];
        //notify self and state change listeners if the datum has changed
        if (previous != datum) {
            notifyListeners(id, datum);
        }
        data[id] = datum;
    }

    /**
     * Retrieves sensor data.
     *
     * @param id numeric identifier of data.
     * @return representative sensor data.
     */
    public double getState(int id) {
        if (id >= data.length || id < 0) {
            return ERROR;
        }
        return data[id];
    }

    /**
     * Returns the number of different data stored by this sensor.
     *
     * @return number of data.
     */
    public int numData() {
        return data.length;
    }

    /**
     * Calls the listener events based on what has changed
     *
     * @param id the key of the data that changed
     * @param newDatum the datum's new value
     */
    protected abstract void notifyListeners(int id, double newDatum);
}
