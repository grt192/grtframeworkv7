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
 * @author Sidd Karamcheti <sidd.karamcheti@gmail.com>
 */
public class WingAuto extends GRTMacroController {

    private double autoShooterAngle = GRTConstants.getValue("anglePyramidBackCenter");
    private double shootingSpeed = GRTConstants.getValue("shootingRPMS");
    private double downAngle = GRTConstants.getValue("shooterDown");
    private double shooterDelay = GRTConstants.getValue("shooterDelay");

    public WingAuto(GRTDriveTrain dt, Shooter shooter, Belts belts,
            ExternalPickup ep, GRTGyro gyro) {
        /**
         * Starting in the back left corner of the pyramid, we first start by
         * firing our 3 starting frisbees. Note: We start facing straight, and
         * then turn to shoot
         */
        System.out.println("Tony's super unrealistic centerline-wing auto activated");

        //lowers pickup
        GRTMacro lowerPickup = new LowerPickup(ep);
        addMacro(lowerPickup);

        //Sets up shooter angle and flywheel speed
        System.out.println("Setting shooter up to shoot ");

        //Turns into shooting position (corner angle)
        double cornerAngle = GRTConstants.getValue("CornerAngle");
        addMacro(new MacroTurn(dt, gyro, cornerAngle, 2000));

        //Shoot our 3 frisbees (4 shots in case of a misfire)
        addMacro(new Shoot(shooter, 500, 4));

        //lowers shooter and starts up EP as it starts driving
        ShooterSet lowerShooter = new ShooterSet(downAngle, 0, shooter, 3500);
        addMacro(lowerShooter);

        AutoPickup startPickup = new AutoPickup(ep, belts, 300);
        addMacro(startPickup);

        //Important Constants
        double wingDriveAngle = GRTConstants.getValue("WingDriveAngle"); // Angle to turn towards centerline
        double wingDriveDistance = GRTConstants.getValue("WingDriveDistance"); // Distance to drive to get to centerline

        double centerPickupAngle = GRTConstants.getValue("centerPickupAngle");  //Absolute angle to center ourselves along the line
        double pickupFrisbeesDriveDistance = GRTConstants.getValue("pickupFrisbeesDriveDistance");  //How far across the center line we want to drive
    }
}
        //Not really necessary, should be set to 90