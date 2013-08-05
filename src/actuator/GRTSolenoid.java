/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package actuator;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * Wrapper class for solenoids. Done to allow for interchangeablity between
 * solenoids on the solenoid module, and solenoids on spikes.
 * 
 * @author Calvin
 */
public class GRTSolenoid {
    
    private Solenoid solenoid;
    
    /**
     * Constructs a solenoid on the default solenoid module.
     * 
     * @param channel Channel on the module. 
     */
    public GRTSolenoid(final int channel) {
        solenoid = new Solenoid(channel);
    }
    
    /**
     * Constructs a solenoid on a solenoid module.
     * 
     * @param moduleNumber Number of solenoid module.
     * @param channel Channel on the module.
     */
    public GRTSolenoid(final int moduleNumber, final int channel) {
        solenoid = new Solenoid(moduleNumber, channel);
    }
    
    /**
     * Package-private constructor for use in GRTDoubleActuator.
     */
    GRTSolenoid() {}
    
    /**
     * Sets the state of the solenoid.
     * 
     * @param on true to activate, false to deactivate 
     */
    public void set(boolean on) {
        solenoid.set(on);
    }
    
    /**
     * Gets the state of the solenoid.
     * 
     * @return true if active, false otherwise 
     */
    public boolean get() {
        return solenoid.get();
    }

    /**
     * Free the solenoid.
     */
    public void free() {
        if (solenoid != null)
            solenoid.free();
    }
}
