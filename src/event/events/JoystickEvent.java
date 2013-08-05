package event.events;

import sensor.GRTJoystick;

/**
 *
 * @author dan
 */
public class JoystickEvent extends SensorEvent {

    public JoystickEvent(GRTJoystick source, int id, double value) {
        super(source, id, value);
    }
}
