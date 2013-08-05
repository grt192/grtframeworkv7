package event.events;

import core.Sensor;

/**
 * Generic potentiometer event. Potentiometers can be standalone sensors,
 * or be part of a larger sensor (i.e. a sensor array such as a ButtonBoard.)
 * 
 * @author Calvin
 */
public class PotentiometerEvent extends SensorEvent {

    /**
     * Creates a new PotentiometerEvent.
     * 
     * @param source source of event
     * @param value amount the potentiometer has turned
     */
    public PotentiometerEvent(Sensor source, double value) {
        this(source, 0, value);
    }
    
    /**
     * Creates a new PotentiometerEvent.
     * 
     * @param source source of event
     * @param id id of potentiometer on sensor with multiple pots
     * @param value amount the potentiometer has turned
     */
    public PotentiometerEvent(Sensor source, int id, double value) {
        super(source, id, value);
    }

    /**
     * Returns how far the potentiometer has been turned.
     * 
     * @return value from 0-1, 0 representing all the way to the left and
     * 1 representing all the way to the right
     */
    public double getValue() {
        return getData();
    }
    
    /**
     * Returns the ID of this potentiometer.
     * 
     * @return id of pot on sensor with multiple
     */
    public int getID() {
        return super.getID();
    }
}
