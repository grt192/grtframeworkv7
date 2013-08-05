package mechanism;

import actuator.GRTSolenoid;
import core.GRTLoggedProcess;

/**
 * Mechanism Code for Climber
 * @author Sidd
 */
public class Climber extends GRTLoggedProcess {
    
    private GRTSolenoid solenoid;
    
    public Climber(GRTSolenoid solenoid) { 
        super("Climber mech");
        this.solenoid = solenoid;
    }
    
    public void raise() {
        solenoid.set(true);
    }
    
    public void lower() {
        solenoid.set(false);
    }
}
