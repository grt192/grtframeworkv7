package deploy;

import actuator.GRTSolenoid;
import controller.*;
import controller.auto.*;
import core.GRTConstants;
import core.GRTMacroController;
import core.SensorPoller;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Victor;
import event.listeners.ConstantUpdateListener;
import logger.GRTLogger;
import mechanism.*;
import sensor.*;

/**
 * Constructor for the main robot. Put all robot components here.
 *
 * @author ajc
 */
public class MainRobot extends GRTRobot implements ConstantUpdateListener {

    //Autonomous mode constants
    private static final int AUTO_MODE_3_FRISBEE = 0;
    private static final int AUTO_MODE_5_FRISBEE = 1;
    private static final int AUTO_MODE_7_FRISBEE = 2;
    private static final int AUTO_MODE_DRIVE_CENTER_LEFT = 3;
    private static final int AUTO_MODE_5_CENTERLINE_FRISBEE = 4;
    private static final int AUTO_MODE_CENTERLINE = 5;
    private static final int AUTO_MODE_DOUCHE = 6;
    private static final int AUTO_MODE_6_FRISBEE = 7;
    private static final int AUTO_MODE_3_CORNER_FRISBEE = 8;
    private static final int AUTO_MODE_WING_AUTO = 9;

    //Private i-vars.
    private GRTDriveTrain dt;
    private Belts belts;
    private Shooter shooter;
    private ExternalPickup ep;
    private Climber climber;
    private GRTGyro gyro;
    private GRTMacroController macroController;
    private int autoMode = AUTO_MODE_3_FRISBEE; //Default autonomous mode

    /**
     * Initializer for the robot. Calls an appropriate initialization function.
     */
    public MainRobot() {

        System.out.println("Robot being instantiated");

        if (GRTConstants.getValue("consoleOutput") == 0.0) {
            GRTLogger.disableLogging();
        }

        double robot = GRTConstants.getValue("robot");
        if (robot == 2013.2) {
            System.out.println("Starting up 2013 OmegaBot");
            omegaInit();
        }

    }

    public void disabled() {
        super.disabled();
        
        GRTLogger.logInfo("Disabling robot. Halting drivetrain");
        dt.setMotorSpeeds(0.0, 0.0);
        shooter.adjustHeight(0);
        shooter.setFlywheelOutput(0);
        ep.stopRaiser();
        ep.stopRoller();
        climber.lower();
        belts.stop();
    }

    /**
     * Initializer for omega bot.
     */
    private void omegaInit() {

        SensorPoller sp = new SensorPoller(10);     //Thread that polls all sensors every 10ms.
        SensorPoller encp = new SensorPoller(50);   //Thread that polls encoders less often, to make speed readings more consistent

        GRTJoystick leftPrimary = new GRTJoystick(1, "left primary joy");
        GRTJoystick rightPrimary = new GRTJoystick(2, "right primary joy");
        GRTXboxJoystick secondary = new GRTXboxJoystick(3, "xbox mech joy");
        sp.addSensor(leftPrimary);
        sp.addSensor(rightPrimary);
        sp.addSensor(secondary);

        GRTLogger.logInfo("Joysticks initialized");

        //Battery Sensor
        GRTBatterySensor batterySensor = new GRTBatterySensor("battery");
        sp.addSensor(batterySensor);

        //Shifter solenoids
        GRTSolenoid leftShifter = new GRTSolenoid(getPinID("leftShifter"));
        GRTSolenoid rightShifter = new GRTSolenoid(getPinID("rightShifter"));

        // PWM outputs
        Talon leftDT1 = new Talon(getPinID("leftDT1"));
        Talon leftDT2 = new Talon(getPinID("leftDT2"));
        Talon rightDT1 = new Talon(getPinID("rightDT1"));
        Talon rightDT2 = new Talon(getPinID("rightDT2"));
        GRTLogger.logInfo("Motors initialized");

        double dtDistancePerPulse = GRTConstants.getValue("DTDistancePerPulse");
        //Mechanisms
        GRTEncoder leftEnc = new GRTEncoder(getPinID("encoderLeftA"),
                getPinID("encoderLeftB"),
                dtDistancePerPulse, true, "leftEnc");
        GRTEncoder rightEnc = new GRTEncoder(getPinID("encoderRightA"),
                getPinID("encoderRightB"),
                dtDistancePerPulse, false, "rightEnc");
        encp.addSensor(leftEnc);
        encp.addSensor(rightEnc);

        dt = new GRTDriveTrain(leftDT1, leftDT2, rightDT1, rightDT2,
                leftShifter, rightShifter,
                leftEnc, rightEnc);

        dt.setScaleFactors(
                GRTConstants.getValue("leftDT1Scale"),
                GRTConstants.getValue("leftDT2Scale"),
                GRTConstants.getValue("rightDT1Scale"),
                GRTConstants.getValue("rightDT2Scale"));

        DriveController dc = new DriveController(dt, leftPrimary, rightPrimary);

        addTeleopController(dc);

        //Compressor
        Compressor compressor = new Compressor(getPinID("compressorSwitch"),
                getPinID("compressorRelay"));
        compressor.start();
        System.out.println("pressure switch=" + compressor.getPressureSwitchValue());

        //shooter
        Talon shooter1 = new Talon(getPinID("shooter1"));
        Talon shooter2 = new Talon(getPinID("shooter2"));
        Talon shooterRaiser = new Talon(getPinID("shooterRaiser"));
        GRTSolenoid shooterFeeder = new GRTSolenoid(getPinID("shooterFeeder"));

        GRTEncoder shooterEncoder = new GRTEncoder(getPinID("shooterEncoderA"),
                getPinID("shooterEncoderB"),
                GRTConstants.getValue("shooterEncoderPulseDistance"),
                "shooterFlywheelEncoder");
        Potentiometer shooterPot = new Potentiometer(
                getPinID("shooterPotentiometer"),
                "shooter potentiometer");
        GRTSwitch lowerShooterLimit = new GRTSwitch(
                getPinID("shooterLowerLimit"),
                true, "lowerShooterLimit");

        shooter = new Shooter(shooter1, shooter2, shooterFeeder,
                shooterRaiser, shooterEncoder, shooterPot, lowerShooterLimit);

        encp.addSensor(shooterEncoder);
        sp.addSensor(shooterPot);

        //Belts
        System.out.println("belts = " + getPinID("belts"));
        System.out.println("rollerMotor = " + getPinID("rollerMotor"));
        System.out.println("raiserMotor = " + getPinID("raiserMotor"));

        Victor beltsMotor = new Victor(getPinID("belts"));

        belts = new Belts(beltsMotor);
        belts.startPolling();


        //PickerUpper
        SpeedController rollerMotor = new Victor(getPinID("rollerMotor"));
        SpeedController raiserMotor = new Victor(getPinID("raiserMotor"));
        GRTSwitch limitUp = new GRTSwitch(getPinID("pickUpUpperLimit"), false, "limitUp");
        GRTSwitch limitDown = new GRTSwitch(getPinID("pickUpLowerLimit"), false, "limitDown");
        sp.addSensor(limitUp);
        sp.addSensor(limitDown);

        ep = new ExternalPickup(rollerMotor, raiserMotor, limitUp, limitDown);

        //Climber
        GRTSolenoid climberSolenoid = new GRTSolenoid(getPinID("climberSolenoid"));
        climber = new Climber(climberSolenoid);

        System.out.println("Mechs created");

        gyro = new GRTGyro(1, "Turning Gyro");
        sp.addSensor(gyro);
        
        //Mechcontroller
        MechController mechController = new MechController(leftPrimary, rightPrimary, secondary,
                shooter, ep, climber, belts, dt, gyro);

        addTeleopController(mechController);

        //Autonomous initializing
        System.out.println("Start macro creation");
        defineAutoMacros();

        GRTConstants.addListener(this);

        sp.startPolling();
        encp.startPolling();
    }

    private int getPinID(String name) {
        return (int) GRTConstants.getValue(name);
    }

    /**
     * Lays out definitions of each auto macro routine. Based on the type of
     * autonomous mode
     */
    private void defineAutoMacros() {
        clearAutoControllers();

        autoMode = (int) GRTConstants.getValue("autoMode");

        GRTLogger.logInfo("Automode num: " + autoMode);
        
        switch (autoMode) {

            case AUTO_MODE_3_FRISBEE:
                System.out.println("Auto mode: 3 frisbee");
                macroController = new ThreeFrisbeeAuto(shooter, dt, gyro);
                break;
            case AUTO_MODE_5_FRISBEE:
                System.out.println("Auto mode: 5 frisbee");
                macroController = new FiveFrisbeeAuto(dt, shooter, belts, ep, gyro);
                break;
            case AUTO_MODE_7_FRISBEE:
                System.out.println("Auto mode: 7 frisbee");
                macroController = new SevenFrisbeeAuto(shooter, dt, gyro, ep, belts);
                break;
            case AUTO_MODE_5_CENTERLINE_FRISBEE:
                System.out.println("Auto mode: 5 frisbee at centerline");
                macroController = new FiveFrisbeeCenterlineAuto(dt, shooter, belts, ep, gyro);
                break;
            case AUTO_MODE_CENTERLINE:
                System.out.println("Auto mode: 7 frisbee at centerline");
                macroController = new CenterlineAuto(dt, shooter, belts, ep, gyro);
                break;
            case AUTO_MODE_DOUCHE:
                System.out.println("Auto mode douche. Go fuck yourself.");
                macroController = new ScumbagAuto(shooter, dt, gyro);
                break;
            case AUTO_MODE_6_FRISBEE:
                System.out.println("Auto mode: 6 frisbee from front");
                macroController = new SixFrisbeeAuto(dt, shooter, belts, ep);
                break;
            case AUTO_MODE_3_CORNER_FRISBEE:
                System.out.println("Auto mode: 3 frisbee from corner");
                macroController = new ThreeFrisbeeCornerAuto(shooter, dt, gyro);
                break;
            case AUTO_MODE_WING_AUTO:
                System.out.println("Auto mode: 5 frisbee wing auto");
                macroController = new WingAuto(dt, shooter, belts, ep, gyro);
            default:    //Do nothing
                macroController = null;
                System.out.println("Auto mode: nothing");
                break;
        }
        
        if (macroController != null) {
            addAutonomousController(macroController);
        }
    }

    public final void updateConstants() {
        defineAutoMacros();
    }
}
