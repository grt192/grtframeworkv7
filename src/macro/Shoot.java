/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package macro;

import core.GRTMacro;
import edu.wpi.first.wpilibj.Timer;
import mechanism.Shooter;

/**
 *
 * @author keshav
 */
public class Shoot extends GRTMacro {
 
    private final Shooter shooter;
    private final int num;
    
    /**
     * Operate Luna for some cycles
     * @param shooter Shooter object
     * @param timeout Timeout (in ms)
     * @param num times to operate luna
     */
    public Shoot(Shooter shooter, int timeout, int num) {
        super("Shoot Macro", timeout);
        this.shooter = shooter;
        this.num = num;
    }

    protected void perform() {
        for (int i = 0; i < num; i++) {
            System.out.println("Shooting a frisbee!");
            shooter.shoot();
            Timer.delay(0.15);
            shooter.unShoot();
            if (i < num - 1)
                Timer.delay(0.50);
        }
        notifyFinished();
    }

    protected void die() {        
    }

    protected void initialize() {
    }

}
