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
import macro.MacroTurn;
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
public class FiveFrisbeeAuto extends GRTMacroController {

    //Preset shooter angles
    private double autoShooterAngle1 = GRTConstants.getValue("anglePyramidBackCenter");
    private double autoShooterAngle2 = GRTConstants.getValue("anglePyramidMiddleCenter");

    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    //Shooter absolute lowest point
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");


    public FiveFrisbeeAuto(GRTDriveTrain dt, Shooter shooter, Belts belts,
            ExternalPickup ep, GRTGyro gyro) {
        /**
         * Okay, five frisbee. Starting in the back right corner of the pyramid,
         * we first start by firing our 3 starting frisbees.
         */
        System.out.println("30 Point Autonomous Activated.");

        double autoDriveDistance = GRTConstants.getValue("auto5Distance");    //Drive angled for 1.80m to pickup the two frisbees centered under the pyramid.
//        double headingAngle = GRTConstants.getValue("headingAngle");
        //lowers pickup
        GRTMacro lowerPickup = new LowerPickup(ep);
        addMacro(lowerPickup);
	
        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");
        addMacro(new ShooterSet(autoShooterAngle1, shootingSpeed, shooter, 2500));
        addMacro(new MacroDelay((int)shooterDelay));
	
        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));

        //lowers shooter and starts up EP as it starts driving
        ShooterSet lowerShooter = new ShooterSet(downAngle, 0, shooter, 3500);
	
        addMacro(lowerShooter);
        AutoPickup startPickup = new AutoPickup(ep, belts, 300);
        addMacro(startPickup);
	
	//Turn around
	addMacro(new MacroTurn(dt, gyro, 180.0, 2000));
        //spins around, drives over frisbees, comes back  
	addMacro(new MacroDrive(dt, autoDriveDistance, 2000));
        addMacro(new MacroDrive(dt, -autoDriveDistance, 2000));
        addMacro(new MacroTurn(dt, gyro, -180.0, 2000));

	//Setup the shooter for our second two frisbees
        addMacro(new ShooterSet(autoShooterAngle2, shootingSpeed, shooter, 2500));	

        addMacro(new Shoot(shooter, 500, 5));
        //spins down shooter and lowers it prior to teleop
        addMacro(new ShooterSet(downAngle, 0, shooter, 1000));
    }
}
