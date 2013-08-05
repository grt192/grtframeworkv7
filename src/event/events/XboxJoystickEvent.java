package event.events;

import sensor.GRTXboxJoystick;

/**
 *
 * @author student
 */
public class XboxJoystickEvent extends SensorEvent {

    public XboxJoystickEvent(GRTXboxJoystick source, int id, double value) {
        super(source, id, value);
    }
}