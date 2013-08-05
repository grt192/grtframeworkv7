/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller.auto;

import core.GRTConstants;
import macro.MacroDrive;
import macro.MacroTurn;
import mechanism.GRTDriveTrain;
import mechanism.Shooter;
import sensor.GRTGyro;

/**
 * What are you looking at?
 * @author Andrew Duffy <gerberduffy@gmail.com>
 * And no, I'm not proud of this.
 */
public class ScumbagAuto extends ThreeFrisbeeAuto {
    
    private double douchebagDistanceToCenter = GRTConstants.getValue("douchebagDistanceToCenter");
    
    public ScumbagAuto(Shooter s, GRTDriveTrain dt, GRTGyro gyro){
        super(s, dt, gyro);
        System.out.println("Douchbag auto constructor");
        addMacro(new MacroDrive(dt, douchebagDistanceToCenter, 2000));
        addMacro(new MacroTurn(dt, gyro, 10 * 1000, 15 * 1000));
    }
    
}
