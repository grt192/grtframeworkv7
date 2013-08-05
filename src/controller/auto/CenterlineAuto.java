/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.auto;

import controller.DeadReckoner;
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
 *
 * @author Andrew Duffy <gerberduffy@gmail.com>
 * @author Calvin Huang <clhuang@eneron.us>
 */
public class CenterlineAuto extends GRTMacroController {
    private double autoShooterAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");


    public CenterlineAuto(GRTDriveTrain dt, Shooter shooter, Belts belts,
            ExternalPickup ep, GRTGyro gyro) {
        /**
         * Starting in the center of the pyramid,
         * we first start by firing our 3 starting frisbees.
         */
        System.out.println("Massive nerd Point Autonomous Activated.");

        //lowers pickup
        GRTMacro lowerPickup = new LowerPickup(ep);
        addMacro(lowerPickup);

        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");

        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));

        //lowers shooter and starts up EP as it starts driving
        ShooterSet lowerShooter = new ShooterSet(downAngle, 0, shooter, 3500);
        addMacro(lowerShooter);

        AutoPickup startPickup = new AutoPickup(ep, belts, 300);
        addMacro(startPickup);
        
        //Important Constants
               double startingY = GRTConstants.getValue("CenterlineInitialY"); //Starting position on the field: Y
        double startingAngle = GRTConstants.getValue("CenterlineStartingAngle"); //Starting heading on the field
        
        //Update the global DeadReckoner with our starting pose.
         double startingX = GRTConstants.getValue("CenterlineInitialX"); //Starting position on the field: X
        DeadReckoner.setPosition(startingX, startingY, startingAngle);

        //Turning constants
        double centerlineDriveAngle  = GRTConstants.getValue("centerlineDriveAngle"); //Absolute Angle we need to turn to get to the centerline
        double centerPickupAngle = GRTConstants.getValue("centerPickupAngle");  //Absolute angle to center ourselves along the line
        double driveToCenter = GRTConstants.getValue("driveToCenter");      //Distance to center line
        double pickupFrisbeesDriveDistance = GRTConstants.getValue("pickupFrisbeesDriveDistance");  //How far across the center line we want to drive
        
        //Begin attempt to perform center pickup.
        System.out.println("Attempting Center Pickup!!!");
        System.out.println("Attempting Center Pickup!!!");
        System.out.println("Attempting Center Pickup!!!");
        
        addMacro(new MacroTurn(dt, gyro, DeadReckoner.turnAngle(centerlineDriveAngle), 3000));  //Turn to the angle that gets us to the left side of the field.
        addMacro(new MacroDrive(dt, driveToCenter, 5000)); //Drive over to the center line
        addMacro(new MacroTurn(dt, gyro, DeadReckoner.turnAngle(centerPickupAngle), 2000));   //Turn to the frisbees
        addMacro(new MacroDrive(dt, pickupFrisbeesDriveDistance, 3000));//Pickup some frisbees. Change distance based on frisbees on field.
        
        addMacro(new ShooterSet(autoShooterAngle, shootingSpeed, shooter, 2500)); //start moving shooter back
        addMacro(new MacroDelay((int)shooterDelay));
        
        double backToPyramidAngle = DeadReckoner.angleFrom(startingX, startingY);
        addMacro(new MacroTurn(dt, gyro, backToPyramidAngle, 2000));    //Turn back to the pyramid
        
        double backToPyramidDistance = DeadReckoner.distanceFrom(startingX, startingY);
        addMacro(new MacroDrive(dt, backToPyramidDistance, 5000));  //Drive back to the pyramid
        
        double turnToShoot = DeadReckoner.turnAngle(startingAngle); //Find the angle we need to turn back to to shoot.
        addMacro(new MacroTurn(dt, gyro, turnToShoot, 2000));
        
        addMacro(new Shoot(shooter, 500, 5));
        
        //spins down shooter and lowers it prior to teleop
        addMacro(new ShooterSet(downAngle, 0, shooter, 1000));
    }
}
