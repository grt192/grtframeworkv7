package sensor;

import core.Sensor;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO;
import edu.wpi.first.wpilibj.DriverStationEnhancedIO.EnhancedIOException;
import event.events.ButtonEvent;
import event.events.PotentiometerEvent;
import event.listeners.ButtonListener;
import event.listeners.PotentiometerListener;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Button board on the 2013 driver station.
 * 
 * @author Calvin
 */
public class ButtonBoard extends Sensor {
    
    public static final int KEY_BUTTON1 = 0;
    public static final int KEY_BUTTON2 = 1;
    public static final int KEY_BUTTON3 = 2;
    public static final int KEY_BUTTON4 = 3;
    public static final int KEY_BUTTON5 = 4;
    public static final int KEY_BUTTON6 = 5;
    public static final int KEY_POT1 = 6;
    public static final int KEY_POT2 = 7;
    
    private static final int[] BUTTON_PINS = {2, 4, 6, 1, 3, 5};
    private static final int[] POT_PINS = {1, 3};
    private static final int[] LED_PINS = {8, 10, 12};
    
    private Vector buttonListeners = new Vector();
    private Vector potentiometerListeners = new Vector();
    
    private static final DriverStationEnhancedIO ioBoard =
            DriverStation.getInstance().getEnhancedIO();
    
    private static final ButtonBoard buttonBoard = new ButtonBoard();
    
    private ButtonBoard() {
        super("Button Board", 8);
        
        try {
            for (int i = 0; i < BUTTON_PINS.length; i++)
                ioBoard.setDigitalConfig(BUTTON_PINS[i], DriverStationEnhancedIO.tDigitalConfig.kInputPullUp);
            
            for (int i = 0; i < LED_PINS.length; i++) {
                ioBoard.setDigitalConfig(LED_PINS[i], DriverStationEnhancedIO.tDigitalConfig.kOutput);
                ioBoard.setDigitalOutput(LED_PINS[i], true);
            }
            
        } catch (EnhancedIOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static ButtonBoard getButtonBoard() {
        return buttonBoard;
    }

    protected void notifyListeners(int id, double newDatum) {
        if (id < 6)
            logInfo("ButtonBoard id: " + id + " datum " + newDatum);
        
        if (id < 6) { //button event
            ButtonEvent e = new ButtonEvent(this, id, newDatum == TRUE);
            if (newDatum == TRUE)
                for (Enumeration en = buttonListeners.elements(); en.hasMoreElements();)
                    ((ButtonListener) en.nextElement()).buttonPressed(e);
            else
                for (Enumeration en = buttonListeners.elements(); en.hasMoreElements();)
                    ((ButtonListener) en.nextElement()).buttonReleased(e);
        } else { //potentiometer event
            PotentiometerEvent e = new PotentiometerEvent(this, id, newDatum);
            for (Enumeration en = potentiometerListeners.elements(); en.hasMoreElements();)
                ((PotentiometerListener) en.nextElement()).valueChanged(e);
        }
    }
    
    protected void poll() {
        for (int i = 0; i < 6; i++) {  //iterate through buttons
            //button state IDs go from 0 through 5
            setState(i, getButtonState(i + 1) ? TRUE : FALSE);
        }
        
        for (int i = 0; i < 2; i++) { //iterate through pot pins
            //pot state IDs go from 6 to 7
            setState(i + 6, getPotentiometerState(i + 1));
        }
    }

    /**
     * Set the state of an LED on the driver station.
     * 
     * @param num number of LED, from 1-3
     * @param on whether or not the LED is on
     */
    public void setLED(int num, boolean on) {
        if (num <= 3 && num >= 1) {
            try {
                ioBoard.setDigitalOutput(LED_PINS[num - 1], !on);
            } catch (EnhancedIOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Get the state of a button.
     * 
     * @param num number of button, from 1-6
     * @return true if the button is pressed, false otherwise
     */
    public boolean getButtonState(int num) {
        if (num <= 6 && num > 0) {
            try {
                return !ioBoard.getDigital(BUTTON_PINS[num - 1]);
            } catch (EnhancedIOException ex) {
                ex.printStackTrace();
            }
        }
        
        return false;
    }
    
    /**
     * Get the state of a potentiometer.
     * 
     * @param num number of the potentiometer, from 1-6
     * @return the ratiometric turn of the potentiometer, from 0-1
     */
    public double getPotentiometerState(int num) {
        if (num <= 2 && num > 0) {
            try {
                return ioBoard.getAnalogInRatio(POT_PINS[num - 1]);
            } catch (EnhancedIOException ex) {
                ex.printStackTrace();
            }
        }
        
        return Double.NaN;
    }
    
    /**
     * Adds a button listener.
     * @param l listener to add
     */
    public void addButtonListener(ButtonListener l) {
        buttonListeners.addElement(l);
    }
    
    /**
     * Removes a button listener
     * @param l listener to remove
     */
    public void removeButtonListener(ButtonListener l) {
        buttonListeners.removeElement(l);
    }
    
    /**
     * Adds a potentiometer listener
     * @param l listener to add
     */
    public void addPotentiometerListener(PotentiometerListener l) {
        potentiometerListeners.addElement(l);
    }
    
    /**
     * Removes a potentiometer listener
     * @param l listener to add
     */
    public void removePotentiometerListener(PotentiometerListener l) {
        potentiometerListeners.removeElement(l);
    }
}
