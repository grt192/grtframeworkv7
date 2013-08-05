package sensor;

import core.Sensor;
import edu.wpi.first.wpilibj.ADXL345_I2C;
import event.events.ADXL345Event;
import event.listeners.ADXL345Listener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Wrapper for the ADXL345 accelerometer. Measures X, Y, and Z accelerations in
 * G's (doesn't account for gravity)
 *
 * @author gerberduffy
 */
public class GRTADXL345 extends Sensor {

    private ADXL345_I2C accelerometer;
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;
    private static final int NUM_DATA = 3;
    private Vector listeners;

    /**
     * Instantiates a new ADXL345.
     *
     * @param moduleNum number of digital module the accelerometer is connected
     * to
     * @param name name of sensor
     */
    public GRTADXL345(int moduleNum, String name) {
        super(name, NUM_DATA);
        accelerometer = new ADXL345_I2C(moduleNum,
                ADXL345_I2C.DataFormat_Range.k2G);

        listeners = new Vector();
    }

    protected void poll() {
        setState(X_AXIS, accelerometer.getAcceleration(ADXL345_I2C.Axes.kX));
        setState(Y_AXIS, accelerometer.getAcceleration(ADXL345_I2C.Axes.kY));
        setState(Z_AXIS, accelerometer.getAcceleration(ADXL345_I2C.Axes.kZ));
    }

    public void addADXL345Listener(ADXL345Listener l) {
        listeners.addElement(l);
    }

    public void removeADXL345Listener(ADXL345Listener l) {
        listeners.removeElement(l);
    }

    protected void notifyListeners(int id, double newDatum) {
        ADXL345Event e = new ADXL345Event(this, id, newDatum);

        switch (id) {
            case X_AXIS: {
                for (Enumeration en = listeners.elements(); en.hasMoreElements();)
                    ((ADXL345Listener) en.nextElement()).XAccelChange(e);
            }

            case Y_AXIS: {
                for (Enumeration en = listeners.elements(); en.hasMoreElements();)
                    ((ADXL345Listener) en.nextElement()).YAccelChange(e);
            }

            case Z_AXIS: {
                for (Enumeration en = listeners.elements(); en.hasMoreElements();)
                    ((ADXL345Listener) en.nextElement()).ZAccelChange(e);
            }
        }
    }
}
