package macro;

import controller.DeadReckoner;
import core.GRTConstants;
import core.GRTMacro;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import event.listeners.ConstantUpdateListener;
import mechanism.GRTDriveTrain;
import sensor.GRTEncoder;

/**
 * Drives straight for a set distance.
 * 
 * @author keshav
 */
public class MacroDrive extends GRTMacro implements ConstantUpdateListener {

    private static GRTDriveTrain dt;
    private static double leftInitialDistance;
    private static double rightInitialDistance;
    private static PIDController DTController;
    private static PIDController straightController;
    private static GRTEncoder leftEncoder;
    private static GRTEncoder rightEncoder;
    private static double speed;
    private static double leftSF = 1;
    private static double rightSF = 1;
    private static double DTP;
    private static double DTI;
    private static double DTD;
    private static double CP;
    private static double CI;
    private static double CD;
    private static double TOLERANCE;
    private static double MAX_MOTOR_OUTPUT;   
    
    private double distance;
    
    private boolean previouslyOnTarget = false;
        
    private static PIDSource DTSource = new PIDSource() {
        public double pidGet() {
            System.out.println("Distance Traveled: "  + -(rightTraveledDistance() + leftTraveledDistance()) / 2);
            return -(rightTraveledDistance() + leftTraveledDistance()) / 2;
        }
    };
    
    private static PIDOutput DTOutput = new PIDOutput() {
        public void pidWrite(double output) {
            speed = output;
            updateMotorSpeeds();
        }
    };
    
    /**
     * Use distance difference, rather than speed difference, to keep
     * robot straight
     */
    private static PIDSource straightSource = new PIDSource() {
        public double pidGet() {
            
            return rightTraveledDistance() - leftTraveledDistance();
        }
    };
    
    private static PIDOutput straightOutput = new PIDOutput() {
        public void pidWrite(double output) {
            double modifier = Math.abs(output);
            System.out.println(output);
            //concise code is better code
            rightSF = 2 - modifier - (leftSF = 1 - (speed * output < 0 ? modifier : 0)); 
            
//            System.out.println("Left Speed: " + leftSF);
//            System.out.println("Right Speed: " + rightSF);
            
            updateMotorSpeeds();
        }
    };
    
    private static void updateMotorSpeeds() {
        System.out.println("Speed: " + speed + "\tleftSF: " + leftSF + "\trightSF: " + rightSF);
        dt.setMotorSpeeds(speed * leftSF, speed * rightSF);
    }
    
    private static double rightTraveledDistance() {
        return rightEncoder.getDistance() - rightInitialDistance;
    }
    
    private static double leftTraveledDistance() {
        return leftEncoder.getDistance() - leftInitialDistance;
    }
    
    {
        DTController = new PIDController(DTP, DTI, DTD, DTSource, DTOutput);
        straightController = new PIDController(CP, CI, CD, straightSource, straightOutput);
        straightController.setOutputRange(0, 1);
    }

    /*
     * Creates a new Driving Macro
     * 
     * @param dt GRTDriveTrain object
     * @param distance distance to travel in meters (assumes travel in straight line)
     * @param timeout time in ms
     */
    public MacroDrive(GRTDriveTrain dt, double distance, int timeout) {
        super("Drive Macro", timeout);
        MacroDrive.dt = dt;
        this.distance = distance;
        MacroDrive.leftEncoder = dt.getLeftEncoder();
        MacroDrive.rightEncoder = dt.getRightEncoder();
                
        updateConstants();
        GRTConstants.addListener(this);
    }

    protected void initialize() {
        leftInitialDistance = leftEncoder.getDistance();
        rightInitialDistance = rightEncoder.getDistance();

        DTController.setSetpoint(distance);
        straightController.setSetpoint(0);

        DTController.enable();
        straightController.enable();
        
        leftSF = rightSF = 1;
        System.out.println("MACRODRIVE is initialized");
    }

    protected void perform() {
        
        System.out.println("DTerror: " + DTController.getError());
        
        if (DTController.onTarget()) {
            System.out.println("On target!");
            if (previouslyOnTarget)
                notifyFinished();
            else
                previouslyOnTarget = true;
        } else {
            previouslyOnTarget = false;
        }
    }

    protected void die() {
        dt.setMotorSpeeds(0, 0);
        DTController.disable();
        straightController.disable();
        DeadReckoner.notifyDrive(getDistanceTraveled());
    }
    
    public double getDistanceTraveled() {
        return (leftTraveledDistance() + rightTraveledDistance()) / 2;
    }

    public final void updateConstants() {
        DTP = GRTConstants.getValue("DMP");
        DTI = GRTConstants.getValue("DMI");
        DTD = GRTConstants.getValue("DMD");
        CP = GRTConstants.getValue("DMCP");
        CI = GRTConstants.getValue("DMCI");
        CD = GRTConstants.getValue("DMCD");
        TOLERANCE = GRTConstants.getValue("DMTol");
        MAX_MOTOR_OUTPUT = GRTConstants.getValue("DMMax");
        
        DTController.setPID(DTP, DTI, DTD);
        straightController.setPID(CP, CI, CD);
        DTController.setAbsoluteTolerance(TOLERANCE);
        DTController.setOutputRange(-MAX_MOTOR_OUTPUT, MAX_MOTOR_OUTPUT);
    }
}
