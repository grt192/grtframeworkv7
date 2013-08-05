/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package macro;

import core.GRTMacro;
import edu.wpi.first.wpilibj.Timer;
import mechanism.ExternalPickup;

/**
 *
 * @author Calvin
 */
public class LowerPickup extends GRTMacro {

    private ExternalPickup ep;
    
    public LowerPickup(ExternalPickup ep) {
        super("Pickup lower macro", 500);
        this.ep = ep;
    }
    
    protected void initialize() {
    }

    protected void perform() {
        ep.lower();
        Timer.delay(0.4);
        ep.stopRaiser();
        notifyFinished();
    }

    protected void die() {
    }
    
}
