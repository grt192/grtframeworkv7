package mechanism;

import actuator.GRTSolenoid;
import core.GRTConstants;
import core.GRTLoggedProcess;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.SpeedController;
import event.events.EncoderEvent;
import event.events.PotentiometerEvent;
import event.events.SwitchEvent;
import event.listeners.ConstantUpdateListener;
import event.listeners.EncoderListener;
import event.listeners.PotentiometerListener;
import event.listeners.SwitchListener;
import sensor.GRTEncoder;
import sensor.GRTSwitch;
import sensor.Potentiometer;

/**
 * Shooter mechanism.
 *
 * @author Calvin
 */
public class Shooter extends GRTLoggedProcess implements PotentiometerListener, EncoderListener, SwitchListener, ConstantUpdateListener {

    private SpeedController shooterMotor1, shooterMotor2;
    private SpeedController raiser;
    private GRTSolenoid feeder;
    private GRTEncoder flywheelEncoder;
    private Potentiometer raiserPot;
    private PIDController raiserController;
    private PIDController flywheelController;
    private boolean lowerSwitchPressed = false;
    /**
     * PID Constants for the raiser. RAISER_TOLERANCE is the absolute error
     * allowed in the raiser angle (in degrees).
     */
    private double RAISER_P;
    private double RAISER_I;
    private double RAISER_D;
    private double RAISER_TOLERANCE;
    /**
     * PID Constants for the flywheel. FLYWHEEL_TOLERANCe is the percent error
     * allowed by the flywheel (i.e. 5.0 -> 5 percent).
     */
    private double FLYWHEEL_P;
    private double FLYWHEEL_I;
    private double FLYWHEEL_D;
    private double FLYWHEEL_TOLERANCE;
    /**
     * The voltage output by the pot at the lowest angle.
     */
    private double tareAngle;
    /**
     * The angular range of the potentiometer.
     */
    private double POT_RANGE;
    private double MAX_ANGLE;
    private double MIN_ANGLE;

    /**
     * Creates a new shooter.
     *
     * @param shooterMotor1
     * @param shooterMotor2
     * @param feeder
     * @param raiser
     * @param flywheelEncoder
     * @param raiserPot
     */
    public Shooter(SpeedController shooterMotor1, SpeedController shooterMotor2,
            GRTSolenoid feeder, SpeedController raiser, GRTEncoder flywheelEncoder,
            Potentiometer raiserPot, GRTSwitch lowerLimit) {
        super("Shooter mech");
        this.feeder = feeder;
        this.shooterMotor1 = shooterMotor1;
        this.shooterMotor2 = shooterMotor2;
        this.raiser = raiser;
        this.flywheelEncoder = flywheelEncoder;
        this.raiserPot = raiserPot;

        updateConstants();
        lowerLimit.addListener(this);
        raiserPot.addListener(this);
        
        GRTConstants.addListener(this);
    }

    /**
     * Sets the output of the speed controllers controlling the flywheel.
     *
     * @param speed flywheel output, from -1 to 1
     */
    public void setFlywheelOutput(double speed) {
        flywheelController.disable();
        shooterMotor1.set(speed);
        shooterMotor2.set(speed);
    }
    //PID sources. This is the input gain that is read in by the controller, and is used to scale the output gains according to your PID function.
    private PIDSource flywheelSource = new PIDSource() {
        public double pidGet() {
            return flywheelEncoder.getRate();
        }
    };
    //Function that is called with the PID output gain. Here, it is being applied to the shooter motor speeds.
    private PIDOutput flywheelOutput = new PIDOutput() {
        public void pidWrite(double d) {
//            System.out.println("Motor output: " + ((int) (d * 1000)) / 1000.0 + " Shooter RPM: " + (int) flywheelEncoder.getRate() + "  Desired: " + flywheelController.getSetpoint());
            shooterMotor1.set(d);
            shooterMotor2.set(d);
        }
    };

    /**
     * Sets the speed of the flywheel.
     *
     * @param speed speed of flywheel, in RPM
     */
    public void setSpeed(double speed) {
        if (speed == 0) {
            setFlywheelOutput(0);
        } else {
            System.out.println("PID settting flywheel speed to " + speed);
            flywheelController.setSetpoint(speed);
            flywheelController.enable();
        }
    }

    /**
     * Sets the speed of the raiser motor.
     *
     * @param velocity motor output from -1 to 1. A neg. number lowers the
     * shooter.
     */
    public void adjustHeight(double velocity) {

        raiserController.disable();
        setRaiserMotorOutput(velocity);
    }

    private void setRaiserMotorOutput(double velocity) {
        if (velocity < 0 && lowerSwitchPressed) {
            return;
        }

        double currentAngle = getShooterAngle();
        if ((velocity > 0 && currentAngle <= MAX_ANGLE)
                || (velocity < 0 && currentAngle >= MIN_ANGLE)
                || (velocity == 0)) {
            raiser.set(-velocity);
        }
    }

    public boolean isSpunUp() {
        return flywheelController.onTarget();
    }

    public boolean isCorrectAngle() {
        return raiserController.onTarget();
    }

    /**
     * Gets the current shooter angle.
     */
    public double getShooterAngle() {
        return (-raiserPot.getValue() * POT_RANGE + tareAngle);
    }
    private PIDSource raiserSource = new PIDSource() {
        public double pidGet() {
            return getShooterAngle();
        }
    };
    private PIDOutput raiserOutput = new PIDOutput() {
        public void pidWrite(double d) {
//            System.out.println("Motor output: " + ((int) (d * 1000)) / 1000.0 + " Raiser angle: " + (int) getShooterAngle() + "  Desired: " + raiserController.getSetpoint());
            setRaiserMotorOutput(d);
        }
    };

    /**
     * Sets the angle of the shooter.
     *
     * @param angle angle of shooter, from 0 to {@value #MAX_ANGLE}
     */
    public void setAngle(double angle) {
        logInfo("Setting Angle to " + angle);
        if (angle < MIN_ANGLE) {
            angle = MIN_ANGLE;
        } else if (angle > MAX_ANGLE) {
            angle = MAX_ANGLE;
        }

        raiserController.setSetpoint(angle);
        raiserController.enable();
    }
    
    /**
     * Increments the shooter angle.
     * Raises shooter if delta > 0, lowers if delta < 0
     * 
     * @param delta desired change in shooter angle
     */
    public void incrementAngle(double delta) {
        setAngle(raiserController.getSetpoint() + delta);
    }

    /**
     * Extends luna.
     */
    public void shoot() {
        if (flywheelController.getSetpoint() == 0) {
            logInfo("Harsha done fucked up.");
        } else {
            logInfo("Here it comes! Firing frisbee.");
            feeder.set(true);
        }
    }

    /**
     * Retracts luna.
     */
    public void unShoot() {
        logInfo("Unshooting!");
        feeder.set(false);
    }
    DriverStationLCD lcd = DriverStationLCD.getInstance();

    public void valueChanged(PotentiometerEvent e) {
//        System.out.println(getShooterAngle());
        lcd.println(DriverStationLCD.Line.kUser1, 1, Double.toString(getShooterAngle()) + " ");
        lcd.updateLCD();
        double currentSpeed = -raiser.get();
        if ((getShooterAngle() <= MIN_ANGLE && currentSpeed < 0)
                || (getShooterAngle() >= MAX_ANGLE && currentSpeed > 0)) {
            raiser.set(0);
        }
    }

    public void rotationStarted(EncoderEvent e) {
        logInfo("Rotation beginning");
    }

    public void degreeChanged(EncoderEvent e) {
    }

    public void distanceChanged(EncoderEvent e) {
    }

    public void rotationStopped(EncoderEvent e) {
    }

    public void rateChanged(EncoderEvent e) {
    }

    public void switchStateChanged(SwitchEvent e) {
        lowerSwitchPressed = e.getState();
        System.out.println("Limit switch state: " + e.getState());
        if (lowerSwitchPressed && raiser.get() > 0) {
            raiser.set(0);
            System.out.println("stopping due to limitswitch");
        }
    }

    public final void updateConstants() {
        RAISER_P = GRTConstants.getValue("shooterRaiserP");
        RAISER_I = GRTConstants.getValue("shooterRaiserI");
        RAISER_D = GRTConstants.getValue("shooterRaiserD");
        RAISER_TOLERANCE = GRTConstants.getValue("raiserTolerance");
        FLYWHEEL_P = GRTConstants.getValue("flywheelP");
        FLYWHEEL_I = GRTConstants.getValue("flywheelI");
        FLYWHEEL_D = GRTConstants.getValue("flywheelD");
        FLYWHEEL_TOLERANCE = GRTConstants.getValue("flywheelTolerance");
        tareAngle = GRTConstants.getValue("tareAngle");
        POT_RANGE = GRTConstants.getValue("raiserPotRange");
        MAX_ANGLE = GRTConstants.getValue("maxRaiserAngle");
        MIN_ANGLE = GRTConstants.getValue("minRaiserAngle");
        
        if (flywheelController != null) {
            flywheelController.disable();
            flywheelController.free();
        }
        
        if (raiserController != null) {
            raiserController.disable();
            raiserController.free();
        }
        
        flywheelController = new PIDController(FLYWHEEL_P, FLYWHEEL_I, FLYWHEEL_D,
                flywheelSource, flywheelOutput);
        flywheelController.setOutputRange(0, 1);
        flywheelController.setPercentTolerance(FLYWHEEL_TOLERANCE);

        raiserController = new PIDController(RAISER_P, RAISER_I, RAISER_D,
                raiserSource, raiserOutput);
        raiserController.setOutputRange(-1, 1);
        raiserController.setAbsoluteTolerance(RAISER_TOLERANCE);
    }
}
