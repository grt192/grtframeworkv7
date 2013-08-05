/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.auto;

import core.GRTConstants;
import core.GRTMacro;
import core.GRTMacroController;
import macro.AutoPickup;
import macro.LowerPickup;
import macro.MacroDelay;
import macro.MacroDrive;
import macro.Shoot;
import macro.ShooterSet;
import mechanism.Belts;
import mechanism.ExternalPickup;
import mechanism.GRTDriveTrain;
import mechanism.Shooter;
import sensor.GRTGyro;

/**
 * Five frisbee auto, goes to center of pyramid
 * @author Andrew Duffy <gerberduffy@gmail.com>
 */
public class FiveFrisbeeCenterlineAuto extends GRTMacroController {

    private double autoShooterAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");

    private double autoDriveDistance = GRTConstants.getValue("auto5Distance");
    
    
    public FiveFrisbeeCenterlineAuto(GRTDriveTrain dt, Shooter shooter, Belts belts,
            ExternalPickup ep, GRTGyro gyro) {
        /**
         * Okay, five frisbee. Starting in the back right corner of the pyramid,
         * we first start by firing our 3 starting frisbees.
         */
        System.out.println("5 Frisbee Centerline Autonomous Activated.");

        //lowers pickup
        addMacro(new LowerPickup(ep));

        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 2500));
        addMacro(new MacroDelay((int)shooterDelay));

        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));

        //lowers shooter and starts up EP as it starts driving
        ShooterSet lowerShooter = new ShooterSet(downAngle, 0, shooter, 3500);

        addMacro(lowerShooter);
        AutoPickup startPickup = new AutoPickup(ep, belts, 300);
        addMacro(startPickup);

        //spins around, drives over frisbees, comes back  
        addMacro(new MacroDrive(dt, autoDriveDistance, 2500));
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 3500));
        addMacro(new MacroDrive(dt, -autoDriveDistance, 2500));

        addMacro(new Shoot(shooter, 500, 3));
        
        //spins down shooter and lowers it prior to teleop
        addMacro(new ShooterSet(downAngle, 0, shooter, 1000));
    }
}
