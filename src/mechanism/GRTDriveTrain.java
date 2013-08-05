package mechanism;

import actuator.GRTSolenoid;
import core.GRTLoggedProcess;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.SpeedController;
import logger.GRTLogger;
import sensor.GRTEncoder;

/**
 * A Drive Train that is capable of
 * shifting, and has rotary encoders.
 *
 * @author andrew, keshav
 */
public class GRTDriveTrain extends GRTLoggedProcess {

    private final SpeedController leftFront;
    private final SpeedController leftBack;
    private final SpeedController rightFront;
    private final SpeedController rightBack;
    private double leftFrontSF = 1;
    private double leftBackSF = 1;
    private double rightFrontSF = -1;
    private double rightBackSF = -1;
    
    private boolean hasShifters = false;
    private GRTSolenoid leftShifter, rightShifter;
    
    private GRTEncoder leftEncoder, rightEncoder;
    
    double power = 1;    //State variable determining if we run at 1/2 power. 
    
    /**
     * Constructs a new drivetrain.
     *
     * @param leftFront left front motor
     * @param leftBack left back motor
     * @param rightFront right front motor
     * @param rightBack right back motor
     */
    public GRTDriveTrain(SpeedController leftFront, SpeedController leftBack,
            SpeedController rightFront, SpeedController rightBack) {
        
        this(leftFront, leftBack, rightFront, rightBack,
                null, null, null, null);
    }
    
    public GRTDriveTrain(SpeedController leftFront, SpeedController leftBack,
            SpeedController rightFront, SpeedController rightBack,
            GRTEncoder leftEncoder, GRTEncoder rightEncoder) {
        
        this(leftFront, leftBack, rightFront, rightBack,
                null, null,
                leftEncoder, rightEncoder);
    }

    public GRTDriveTrain(SpeedController leftFront, SpeedController leftBack,
            SpeedController rightFront, SpeedController rightBack,
            GRTSolenoid leftShifter, GRTSolenoid rightShifter) {

        this(leftFront, leftBack, rightFront, rightBack,
                leftShifter, rightShifter,
                null, null);
    }
    
    public GRTDriveTrain(SpeedController leftFront, SpeedController leftBack,
            SpeedController rightFront, SpeedController rightBack,
            GRTSolenoid leftShifter, GRTSolenoid rightShifter,
            GRTEncoder leftEncoder, GRTEncoder rightEncoder) {
        
        super("Drivetrain");
        
        this.leftFront = leftFront;
        this.rightFront = rightFront;
        this.leftBack = leftBack;
        this.rightBack = rightBack;
        
        if(leftShifter != null && rightShifter != null) {
            this.hasShifters = true;
            this.leftShifter = leftShifter;
            this.rightShifter = rightShifter;
        }
        
        if(leftEncoder != null && rightEncoder != null) {
            this.leftEncoder = leftEncoder;
            this.rightEncoder = rightEncoder;
//            leftP = GRTConstants.getValue("DTLeftP");
//            leftI = GRTConstants.getValue("DTLeftI");
//            leftD = GRTConstants.getValue("DTLeftD");
//            rightP = GRTConstants.getValue("DTRightP");
//            rightI = GRTConstants.getValue("DTRightI");
//            rightD = GRTConstants.getValue("DTRightD");
//            
//            leftController = new PIDController(leftP, leftI, leftD,
//                    leftSource, leftOutput);
//            rightController = new PIDController(rightP, rightI, rightD,
//                    rightSource, rightOutput);
//            
//            leftController.setOutputRange(-1, 1);
//            rightController.setOutputRange(-1, 1);
        }
    }
    
    /**
     * Depending on robot orientation, drivetrain configuration, controller
     * configuration, motors on different parts of the drivetrain may need to be
     * driven in differing directions. These "scale factor" numbers change the
     * magnitude and/or direction of the different motors; they are multipliers
     * for the speed fed to the motors.
     *
     * @param leftFrontSF left front scale factor.
     * @param leftBackSF left back scale factor.
     * @param rightFrontSF right front scale factor.
     * @param rightBackSF right back scale factor.
     */
    public void setScaleFactors(double leftFrontSF, double leftBackSF,
            double rightFrontSF, double rightBackSF) {
        this.leftFrontSF = leftFrontSF;
        this.leftBackSF = leftBackSF;
        this.rightFrontSF = rightFrontSF;
        this.rightBackSF = rightBackSF;
    }
    
    /**
     * Set the left and right side speed controller
     * output for the drivetrain motors.
     *
     * @param leftVelocity left drivetrain velocity, from -1.0 - 1.0
     * @param rightVelocity right drivetrain velocity, from -1.0 - 1.0
     */
    public void setMotorSpeeds(double leftVelocity, double rightVelocity) {
        logInfo("Left: " + leftVelocity + "\tRight: " + rightVelocity);

        leftFront.set(leftVelocity * leftFrontSF * power);
        leftBack.set(leftVelocity * leftBackSF * power);

        rightBack.set(rightVelocity * rightBackSF * power);
        rightFront.set(rightVelocity * rightFrontSF * power);
    }
        
    /**
     * Set the relative power output of the drivetrain
     *
     * @param power Percentage of power output (double between 0 and 1)
     */
    public void setPower(double power){
        if(power > 1) {
            this.power = 1;
        } else if (power < 0) {
            this.power = 0;
        } else {
            this.power = power;
        }
        logInfo("Power: " + this.power);
    }
    
    public void setFullPower() {
        this.power = 1;
    }
    
    public void shiftUp(){
        GRTLogger.logInfo("Shifting up");
        if(hasShifters){
            leftShifter.set(false);
            rightShifter.set(false); 
        }
    }
    
    public void shiftDown(){
        GRTLogger.logInfo("Shifting down");
        if(hasShifters){
            leftShifter.set(true);
            rightShifter.set(true);
        }
    }
    
    public GRTEncoder getLeftEncoder(){
        return leftEncoder;
    }
    
    public GRTEncoder getRightEncoder(){
        return rightEncoder;
    }
}
