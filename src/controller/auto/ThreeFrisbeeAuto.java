/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.auto;

import core.GRTConstants;
import core.GRTMacroController;
import macro.MacroDelay;
import macro.Shoot;
import macro.ShooterSet;
import mechanism.GRTDriveTrain;
import mechanism.Shooter;
import sensor.GRTGyro;

/**
 *
 * @author Calvin Huang <clhuang@eneron.us>
 */
public class ThreeFrisbeeAuto extends GRTMacroController {

    private double autoShooterAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");
    private double offsetAngle = GRTConstants.getValue("offset3Angle");

    public ThreeFrisbeeAuto(Shooter shooter, GRTDriveTrain dt, GRTGyro gyro) {
        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");
	//addMacro(new MacroTurn(dt, gyro, offsetAngle, 3000));
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 2500));
        addMacro(new MacroDelay((int)shooterDelay));
        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));
        addMacro(new ShooterSet(downAngle, 0, shooter, 2500));
    }
    
}
