package mechanism;

import core.GRTConstants;
import core.GRTLoggedProcess;
import edu.wpi.first.wpilibj.SpeedController;

/**
 * Mechanism code for the Belts past the flipper. (i.e not PickUp Belts)
 * @author Sidd
 */
public class Belts extends GRTLoggedProcess{
    private static final double SPEED = -1.0;
    private final SpeedController beltsMotor;
    private long beltsLastUsed = System.currentTimeMillis();
    private long beltRunTime = (int)(GRTConstants.getValue("beltRunTime") * 1000);
    private boolean runningBelts = false;
    
    public Belts(SpeedController beltsMotor) {
        super("Belts mech", 500);
        this.beltsMotor = beltsMotor;
    }
    
    public void poll() {
        if(System.currentTimeMillis() - beltsLastUsed > beltRunTime && !runningBelts && beltsMotor.get() != 0)
        {
            beltsMotor.set(0);
        }
    }
    
    public void moveUp() {
        runningBelts = true;
        logInfo("Belts moving up!");
        beltsMotor.set(SPEED);
    }
    
    public void moveDown() {
        runningBelts = true;
        logInfo("Belts moving down!");
        beltsMotor.set(-SPEED);
    }
    
    public void stop() {
        logInfo("Belts stopping!");
        runningBelts = false;
        beltsLastUsed = System.currentTimeMillis();
    }   
}
