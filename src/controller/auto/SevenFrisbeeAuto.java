/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.auto;

import core.GRTConstants;
import core.GRTMacroController;
import macro.AutoPickup;
import macro.LowerPickup;
import macro.MacroTurn;
import macro.MacroDelay;
import macro.MacroDrive;
import macro.Shoot;
import macro.ShooterSet;
import mechanism.Belts;
import mechanism.ExternalPickup;
import mechanism.Shooter;
import mechanism.GRTDriveTrain;
import sensor.GRTGyro;

/**
 *
 * @author Calvin Huang <clhuang@eneron.us>
 */
public class SevenFrisbeeAuto extends GRTMacroController {

    private double autoShooterAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");
//    private double offsetAngle = GRTConstants.getValue("offset3Angle");
    private double centerDistance = GRTConstants.getValue("7autoPyramidDistance");
    private double extendedDistance = GRTConstants.getValue("7autoExtendedDistance");
//    private double shakeAngle = GRTConstants.getValue("shakeAngle");

    public SevenFrisbeeAuto(Shooter shooter, GRTDriveTrain dt, GRTGyro gyro, ExternalPickup ep, Belts belts) {
        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");
        //addMacro(new MacroTurn(dt, gyro, offsetAngle, 3000));
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 2500));
        addMacro(new LowerPickup(ep));
        addMacro(new MacroDelay((int) shooterDelay));
        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));
        addMacro(new ShooterSet(downAngle, 0, shooter, 2500));


        //Turn around
        addMacro(new MacroTurn(dt, gyro, 180.0, 3000));

        addMacro(new AutoPickup(ep, belts, 500));

        //Drive to the center of the pyramid
        addMacro(new MacroDrive(dt, centerDistance, 2000));
        //Shake to pick stuff up.
        //        addMacro(new MacroTurn(dt, gyro, shakeAngle, 1500));
        //        addMacro(new MacroTurn(dt, gyro, -2 * shakeAngle, 1500));
        //        addMacro(new MacroTurn(dt, gyro, shakeAngle, 1500));

        //Drive to the last pair of frisbees
        addMacro(new MacroDrive(dt, extendedDistance, 2000));
        //Shake to pick stuff up.
        //        addMacro(new MacroTurn(dt, gyro, shakeAngle, 2000));
        //        addMacro(new MacroTurn(dt, gyro, -shakeAngle, 2000));
        //Drive back to where we started.
        addMacro(new MacroDrive(dt, -centerDistance - extendedDistance, 2000));
        addMacro(new MacroTurn(dt, gyro, 180.0, 1500));
        //Time to blow our load.
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 2500));
        addMacro(new LowerPickup(ep));
        addMacro(new MacroDelay((int) shooterDelay));
        //Shoot our 4 frisbees (5 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));

    }
}
