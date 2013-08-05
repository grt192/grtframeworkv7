package sensor;

import core.Sensor;
import edu.wpi.first.wpilibj.AnalogChannel;
import event.events.PotentiometerEvent;
import event.listeners.PotentiometerListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Linear potentiometer on analog channel.
 *
 * @author calvin
 */
public class Potentiometer extends Sensor {

    private static final int FILTER_AVG_NUM = 1;
    private double[] previousValues = new double[FILTER_AVG_NUM];
    private int pollNum;
    
    public static final int KEY_VALUE = 0;
    public static final int NUM_DATA = 1;
    private AnalogChannel channel;
    private Vector potentiometerListeners = new Vector();

    /**
     * Instantiates a new potentiometer on the default analog channel.
     *
     * @param channel channel on analog module potentiometer is connected to
     * @param name name of potentiometer
     */
    public Potentiometer(int channel, String name) {
        super(name, NUM_DATA);
        this.channel = new AnalogChannel(channel);
    }

    /**
     * Instantiates a new potentiometer.
     *
     * @param moduleNum analog module number
     * @param channel channel on analog module potentiometer is connected to
     * @param name name of potentiometer
     */
    public Potentiometer(int moduleNum, int channel,
            String name) {
        super(name, NUM_DATA);
        this.channel = new AnalogChannel(channel);
    }

    protected void poll() {
        previousValues[pollNum++ % FILTER_AVG_NUM] = getUnfilteredValue();
        setState(KEY_VALUE, getValue());
    }

    /**
     * Returns how far the potentiometer has been turned.
     * 
     * @return value from 0-1, 0 representing all the way to the left and
     * 1 representing all the way to the right
     */
    public double getValue() {
        double temp = 0;
        for (int i = 0; i < FILTER_AVG_NUM; i++)
            temp += previousValues[i];
        return temp/FILTER_AVG_NUM;
    }
    
    public double getUnfilteredValue() {
        return channel.getVoltage() / 5.0;
    }

    protected void notifyListeners(int id, double newDatum) {
        PotentiometerEvent e = new PotentiometerEvent(this, newDatum);
        for (Enumeration en = potentiometerListeners.elements();
                en.hasMoreElements();)
            ((PotentiometerListener) en.nextElement()).valueChanged(e);
    }

    public void addListener(PotentiometerListener l) {
        potentiometerListeners.addElement(l);
    }

    public void removeListener(PotentiometerListener l) {
        potentiometerListeners.removeElement(l);
    }
}
