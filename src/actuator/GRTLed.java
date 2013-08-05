package actuator;

import edu.wpi.first.wpilibj.PWM;

/**
 * Abstraction of a standard LED.
 *
 * @author gerberduffy
 */
public class GRTLed {

    private static final int MAX_BRIGHTNESS = 255;
    private static final int OFF_BRIGHTNESS = 0;
    private final PWM led;      //The PWM input controlling the LED
    private int brightness = 0;

    /**
     * Instantiates an LED.
     *
     * @param moduleNum digital module number
     * @param channel channel LED is attached to
     * @param name name of LED
     */
    public GRTLed(int moduleNum, int channel, String name) {

        led = new PWM(moduleNum, channel);
        led.setRaw(brightness);
    }

    /**
     * Instantiates an LED on the default digital module.
     *
     * @param channel channel LED is attached to
     * @param name name of LED
     */
    public GRTLed(int channel, String name) {

        led = new PWM(channel);
        led.setRaw(brightness);
    }

    /**
     * Sets the brightness of this LED
     *
     * @param brightness brightness of LED by varying duty cycle (0-255)
     */
    public void setBrightness(int brightness) {
            if (brightness > MAX_BRIGHTNESS) {
                brightness = MAX_BRIGHTNESS;
            } else if (brightness < OFF_BRIGHTNESS) {
                brightness = OFF_BRIGHTNESS;
            }

            led.setRaw(brightness);

            this.brightness = brightness;
        
    }

    /**
     * Turns LED off if it is on, and vice versa.
     */
    public void toggleState() {
            if (isOn()) {
                led.setRaw(OFF_BRIGHTNESS);
                brightness = OFF_BRIGHTNESS;

            } else {
                led.setRaw(MAX_BRIGHTNESS);
                brightness = MAX_BRIGHTNESS;
            }
        
    }

    /**
     * Gives the current brightness of the LED
     *
     * @return brightness (0-255)
     */
    public int getBrightness() {
        return this.brightness;
    }

    /**
     * Returns true if brightness is greater than 0.
     *
     * @return true if on, false if off
     */
    public boolean isOn() {
        return this.brightness > OFF_BRIGHTNESS;
    }

}
