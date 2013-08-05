package mechanism;

import core.GRTLoggedProcess;
import edu.wpi.first.wpilibj.SpeedController;
import event.events.SwitchEvent;
import event.listeners.SwitchListener;
import sensor.GRTSwitch;

/**
 * Mechanism code for ExternalPickup
 *
 * @author Sidd and Nadia
 */
public class ExternalPickup extends GRTLoggedProcess implements SwitchListener {

    private SpeedController rollerMotor;
    private SpeedController raiserMotor;
    private GRTSwitch limitUp;
    private GRTSwitch limitDown;
    private static double ROLLER_SF = 1, RAISE_SPEED = -0.5, LOWER_SPEED = .5;

    public ExternalPickup(SpeedController rollerMotor, SpeedController raiserMotor,
            GRTSwitch limitUp, GRTSwitch limitDown) {
        super("PickerUpper mech");
        this.rollerMotor = rollerMotor;
        this.raiserMotor = raiserMotor;
        this.limitUp = limitUp;
        this.limitDown = limitDown;

        limitDown.addListener(this);
        limitUp.addListener(this);
    }

    public void raise() {
        //System.out.println("raising ep");
        //if the chalupa is already raised it will not raise further
        if (!limitUp.isPressed()) {
            //System.out.println("really raising ep");
            raiserMotor.set(RAISE_SPEED);
        }
    }

    public void lower() {
        //System.out.println("lowering ep");
        //if the chalupa is already lowered it will not lower further
        if (!limitDown.isPressed()) {
            //System.out.println("really lowering ep");
            raiserMotor.set(LOWER_SPEED);
        }
    }
    public void lower(double speed) {
        //System.out.println("lowering ep");
        //if the chalupa is already lowered it will not lower further
            raiserMotor.set(speed);
    }

    public void pickUp() {
        rollerMotor.set(1 * ROLLER_SF);
        lower(0.30);    //Constantly lower the EP to put compression on frisbees                
    }

    public void spitOut() {
        rollerMotor.set(-1 * ROLLER_SF);
        stopRaiser();
    }

    public void stopRoller() {
        rollerMotor.set(0);
        stopRaiser();
    }

    public void stopRaiser() {
        //System.out.println("stopping ep raiser");
        raiserMotor.set(0); 
    }

    public void switchStateChanged(SwitchEvent e) {
        logInfo("limit switch pressed");
        if (e.getData() == GRTSwitch.TRUE) {
            if (e.getSource() == limitUp) {
                stopRaiser();
            } else if (e.getSource() == limitDown) {
                stopRaiser();
            }
        }
    }
}
