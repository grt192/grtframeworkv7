package sensor;

import core.Sensor;
import edu.wpi.first.wpilibj.Joystick;
import event.events.ButtonEvent;
import event.events.XboxJoystickEvent;
import event.listeners.ButtonListener;
import event.listeners.XboxJoystickListener;
import java.util.Vector;

/**
 *
 * @author ajc
 */
public class GRTXboxJoystick extends Sensor {

    /**
     * Keys of data
     */
    public static final int KEY_BUTTON_A = 0;
    public static final int KEY_BUTTON_B = 1;
    public static final int KEY_BUTTON_X = 2;
    public static final int KEY_BUTTON_Y = 3;
    public static final int KEY_BUTTON_LEFT_SHOULDER = 4;
    public static final int KEY_BUTTON_RIGHT_SHOULDER = 5;
    public static final int KEY_BUTTON_BACK = 6;
    public static final int KEY_BUTTON_START = 7;
    public static final int KEY_BUTTON_LEFT_STICK_DOWN = 8;
    public static final int KEY_BUTTON_RIGHT_STICK_DOWN = 9;
    public static final int KEY_LEFT_X = 10;
    public static final int KEY_LEFT_Y = 11;
    public static final int KEY_RIGHT_X = 12;
    public static final int KEY_RIGHT_Y = 13;
    public static final int KEY_JOYSTICK_ANGLE = 14;
    public static final int KEY_TRIGGER = 15;
    public static final int KEY_PAD = 16;
    private static final int NUM_DATA = 17;
    private static final int NUM_OF_BUTTONS = 10;
    /**
     * State definitions
     */
    public static final double PRESSED = TRUE;
    public static final double RELEASED = FALSE;
    private final Joystick joystick;
    private final Vector buttonListeners;
    private final Vector joystickListeners;
    
    private static final double DEAD_ZONE = 0.07;

    public GRTXboxJoystick(int channel, String name) {
        super(name, NUM_DATA);
        joystick = new Joystick(channel);

        buttonListeners = new Vector();
        joystickListeners = new Vector();
    }

    protected void poll() {
        for (int i = 0; i < NUM_OF_BUTTONS; i++) {
            //if we measure true, this indicates pressed state
            setState(i, joystick.getRawButton(i + 1) ? PRESSED : RELEASED);
        }
        
        setState(KEY_LEFT_X, calcDeadZone(joystick.getX()));
        setState(KEY_LEFT_Y, calcDeadZone(joystick.getY()));
        setState(KEY_RIGHT_X, calcDeadZone(joystick.getRawAxis(4)));
        setState(KEY_RIGHT_Y, calcDeadZone(joystick.getRawAxis(5)));
        setState(KEY_JOYSTICK_ANGLE, joystick.getDirectionRadians());
        setState(KEY_TRIGGER, joystick.getZ());
        setState(KEY_PAD, joystick.getRawAxis(6));
    }
    
    private double calcDeadZone(double y) {
        boolean negative = y < 0;
        y = Math.abs(y);
        if (y <= DEAD_ZONE){
            y = 0.0;
        } else {
            y = (y - DEAD_ZONE) / (1 - DEAD_ZONE);
        }
        
        return negative ? -y : y;
    }

    protected void notifyListeners(int id, double newDatum) {
        if (id <= NUM_OF_BUTTONS) {
            //ID maps directly to button ID
            ButtonEvent e = new ButtonEvent(this, id, newDatum == PRESSED);
            if (newDatum == PRESSED) { //true
                for (int i = 0; i < buttonListeners.size(); i++) {
                    ((ButtonListener) buttonListeners.elementAt(i)).buttonPressed(e);
                }
            } else {
                for (int i = 0; i < buttonListeners.size(); i++) {
                    ((ButtonListener) buttonListeners.elementAt(i)).buttonReleased(e);
                }
            }

        } else { //we are now a joystick
            //only reach here if not a button
            XboxJoystickEvent e = new XboxJoystickEvent(this, id, newDatum);

            //call various events based on which datum we are
            switch (id) {
                case KEY_LEFT_X: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).leftXAxisMoved(e);
                    }

                }
                case KEY_LEFT_Y: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).leftYAxisMoved(e);
                    }
                    break;
                }
                case KEY_RIGHT_X: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).rightXAxisMoved(e);
                    }
                    break;
                }
                case KEY_RIGHT_Y: {
                    e = new XboxJoystickEvent(this, id, -newDatum); //account for xbox being fucking weird
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).rightYAxisMoved(e);
                    }
                    break;
                }
                case KEY_JOYSTICK_ANGLE: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).leftAngleChanged(e);
                    }
                    break;
                }
                case KEY_TRIGGER: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).triggerMoved(e);
                    }
                    break;
                }
                case KEY_PAD: {
                    for (int i = 0; i < joystickListeners.size(); i++) {
                        ((XboxJoystickListener) joystickListeners.elementAt(i)).padMoved(e);
                    }
                    break;
                }

            }
        }


    }

    public void addButtonListener(ButtonListener b) {
        buttonListeners.addElement(b);
    }

    public void removeButtonListener(ButtonListener b) {
        buttonListeners.removeElement(b);
    }

    public void addJoystickListener(XboxJoystickListener l) {
        joystickListeners.addElement(l);
    }

    public void removeJoystickListener(XboxJoystickListener l) {
        joystickListeners.removeElement(l);
    }
}
