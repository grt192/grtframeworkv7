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

/**
 *
 * @author Andrew Duffy <gerberduffy@gmail.com>
 */
public class SixFrisbeeAuto extends GRTMacroController {
    
    private double autoBackPyramidAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double autoCenterlineAngle = GRTConstants.getValue("angleCenterline");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");

    private double autoDriveDistanceToBackPyramid = GRTConstants.getValue("auto6PyramidDistance");
    private double autoDriveDistanceToCenter = GRTConstants.getValue("auto6CenterDistance");
    
    public SixFrisbeeAuto(GRTDriveTrain dt, Shooter shooter, Belts belts,
            ExternalPickup ep) {
        /**
         * Okay, six frisbee. Starting in the front of the pyramid,
         * we first start by firing our 2 starting frisbees.
         */
        System.out.println("6 Frisbee Centerline Autonomous Activated.");

        //lowers pickup
        addMacro(new LowerPickup(ep));

        //lowers shooter and starts up EP as it starts driving
        addMacro(new ShooterSet(downAngle, 0, shooter, 3500));

        addMacro(new AutoPickup(ep, belts, 300));
        
        addMacro(new MacroDelay((int)shooterDelay));

        //drives to back of pyramid.
        addMacro(new MacroDrive(dt, autoDriveDistanceToBackPyramid, 4000));
        
        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");
        addMacro(new ShooterSet(autoBackPyramidAngle, shootingSpeed, shooter, 2500));
        addMacro(new MacroDelay((int)shooterDelay));

        //Shoot our 4 frisbees (3 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 6));
        
        //spins down shooter and lowers it prior to pickup
        addMacro(new ShooterSet(downAngle, 0, shooter, 1000));
        
        //Drives to the centerline.
        addMacro(new MacroDrive(dt, autoDriveDistanceToCenter, 4000));
        addMacro(new ShooterSet(autoCenterlineAngle, shootingSpeed, shooter, 3500));
        addMacro(new MacroDelay((int)shooterDelay));
        
        //Shoot the last 2 frisbees (3 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 3));
    }
}
