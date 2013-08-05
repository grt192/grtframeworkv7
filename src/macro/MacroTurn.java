package macro;

import controller.DeadReckoner;
import core.GRTConstants;
import core.GRTMacro;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import event.listeners.ConstantUpdateListener;
import mechanism.GRTDriveTrain;
import sensor.GRTGyro;

/**
 * Macro that automatically turns the robot a certain angle.
 *
 * @author Calvin
 */
public class MacroTurn extends GRTMacro implements ConstantUpdateListener{

    private double targetAngle;
    private double startAngle;
    private final double turnAngle;
    private static GRTGyro gyro;
    private static GRTDriveTrain dt;
    private static PIDController controller;
    private double P;
    private double I;
    private double D;

    private boolean previouslyOnTarget = false;
    
    private static PIDSource pidSource = new PIDSource() {
        public double pidGet() {
            return gyro.getAngle();
        }
    };
    private static PIDOutput pidOutput = new PIDOutput() {
        public void pidWrite(double output) {
            dt.setMotorSpeeds(output, -output);
        }
    };
    
    {
        controller = new PIDController(0, 0, 0, pidSource, pidOutput, 0.01);
    }
    /**
     * Creates a new turning macro, that turns a set number of degrees.
     * 
     * @param turnAngle angle to turn, in degrees
     * @param gyro gyroscope to track robot movement
     * @param dt drivetrain to command
     */
    public MacroTurn(GRTDriveTrain dt, GRTGyro gyro, double turnAngle, int timeout) {
        super("Turn Macro", timeout, 50);
        
        MacroTurn.dt = dt;
        this.turnAngle = turnAngle;
        MacroTurn.gyro = gyro;
               
        updateConstants();
        GRTConstants.addListener(this);
    }

    protected void perform() {
        System.out.println("Turn macro current angle: " + (gyro.getAngle() - startAngle));
        if (controller.onTarget()) {
            System.out.println("On target");
            if (previouslyOnTarget) {
                System.out.println("Done turning");
                notifyFinished();
            } else
                previouslyOnTarget = true;
        } else
            previouslyOnTarget = false;
    }

    protected void die() {
        System.out.println("Killing turn macro");
        controller.disable();
        dt.shiftUp();
        DeadReckoner.notifyTurn(getAngleTurned());  //Notify of our last heading
    }
    
    public void initialize() {
        dt.shiftDown();
        startAngle = gyro.getAngle();
        targetAngle = startAngle + turnAngle;
        controller.setOutputRange(-1, 1);
        controller.setAbsoluteTolerance(GRTConstants.getValue("TMTol"));
        controller.setSetpoint(targetAngle);
        controller.enable();
    }

    public final void updateConstants() {
        
        P = GRTConstants.getValue("TMP");
        I = GRTConstants.getValue("TMI");
        D = GRTConstants.getValue("TMD");
        
        controller.setPID(P, I, D);
    }
    
    public double getAngleTurned() {
        return gyro.getAngle() - startAngle;
    }
}
