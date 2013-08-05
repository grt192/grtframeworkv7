/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package macro;

import core.GRTMacro;
import mechanism.Shooter;

/**
 *
 * @author keshav
 */
public class ShooterSet extends GRTMacro {
 
    private final double angle;
    private final double speed;
    private Shooter shooter;
    
    /**
     * Shooter angle
     * @param angle Desired angle
     * @param speed
     * @param shooter Shooter object
     * @param timeout Timeout (in ms)
     */
    public ShooterSet(double angle, double speed, Shooter shooter, int timeout) {
        super("Shooter Angle Macro", timeout);
        this.shooter = shooter;
        this.angle = angle;
        this.speed = speed;
    }

    protected void perform() {
        shooter.setAngle(angle);
        shooter.setSpeed(speed);
        notifyFinished();
    }

    protected void die() {
    }

    protected void initialize() {
    }

}
