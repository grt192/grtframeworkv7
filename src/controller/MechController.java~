package controller;

import core.EventController;
import core.GRTConstants;
import edu.wpi.first.wpilibj.Timer;
import event.events.ButtonEvent;
import event.events.JoystickEvent;
import event.events.PotentiometerEvent;
import event.events.XboxJoystickEvent;
import event.listeners.ButtonListener;
import event.listeners.ConstantUpdateListener;
import event.listeners.GRTJoystickListener;
import event.listeners.PotentiometerListener;
import event.listeners.XboxJoystickListener;
import mechanism.Belts;
import mechanism.Climber;
import mechanism.ExternalPickup;
import mechanism.GRTDriveTrain;
import mechanism.Shooter;
import sensor.GRTJoystick;
import sensor.GRTXboxJoystick;

/**
 * Controller for shooter, picker-upper, internal belts, climbing
 *
 * @author Calvin, agd
 */
public class MechController extends EventController implements GRTJoystickListener, XboxJoystickListener, PotentiometerListener, ButtonListener,
        ConstantUpdateListener {

    private GRTJoystick leftJoy;
    private GRTJoystick rightJoy;
    private GRTXboxJoystick secondary;
    private Belts belts;
    private Climber climber;
    private ExternalPickup pickerUpper;
    private Shooter shooter;
    private GRTDriveTrain dt;
    private double shootingSpeed;
    private double shooterPresetY;
    private double shooterPresetB;
    private double shooterDown;
    private double turningDivider;
    private double storedAngle;
    private int padPosition = 0; //-1 for left, 1 for right, 0 for center
    private double DEG_INCREMENT = 0.75;
    private boolean canShoot = true;
    private boolean xBoxBeltsRunning = false;
    private boolean joystickBeltsRunning = false;
    private double shotDelay = 0.5;

    public MechController(GRTJoystick leftJoy, GRTJoystick rightJoy,
            GRTXboxJoystick secondary,
            Shooter shooter, ExternalPickup pickerUpper,
            Climber climber, Belts belts,
            GRTDriveTrain dt) {
        super("Mechanism Controller");
        this.leftJoy = leftJoy;
        this.rightJoy = rightJoy;
        this.secondary = secondary;

        this.belts = belts;
        this.climber = climber;
        this.pickerUpper = pickerUpper;
        this.shooter = shooter;

        this.dt = dt;

        GRTConstants.addListener(this);

        updateConstants();
    }

    protected void startListening() {
        leftJoy.addJoystickListener(this);
        leftJoy.addButtonListener(this);

        rightJoy.addJoystickListener(this);
        rightJoy.addButtonListener(this);

        secondary.addJoystickListener(this);
        secondary.addButtonListener(this);
    }

    protected void stopListening() {
        leftJoy.removeJoystickListener(this);
        leftJoy.removeButtonListener(this);

        rightJoy.removeJoystickListener(this);
        rightJoy.removeButtonListener(this);

        secondary.removeJoystickListener(this);
        secondary.removeButtonListener(this);


        //Set the flywheel controller back to zero on disable. Helps prevent the I term from accumulating to quickly
        shooter.setFlywheelOutput(0.0);

    }

    public void XAxisMoved(JoystickEvent e) {
    }

    public void YAxisMoved(JoystickEvent e) {
    }

    public void AngleChanged(JoystickEvent e) {
    }

    //commented out code is because betabot is FUBAR
    public void buttonPressed(ButtonEvent e) {
        try {
            if (e.getSource() == rightJoy) {
                switch (e.getButtonID()) {
                    case GRTJoystick.KEY_BUTTON_3:
                        joystickBeltsRunning = true;
                        pickerUpper.pickUp();
                        belts.moveUp();
                        break;
                    case GRTJoystick.KEY_BUTTON_2:
                        joystickBeltsRunning = true;
                        pickerUpper.spitOut();
                        belts.moveDown();
                        break;

                    case GRTJoystick.KEY_BUTTON_4:
                        pickerUpper.raise();
                        break;
                    case GRTJoystick.KEY_BUTTON_5:
                        pickerUpper.lower();
                        break;
                }
            } else if (e.getSource() == leftJoy) {
                switch (e.getButtonID()) {
                    case GRTJoystick.KEY_BUTTON_3:
                        climber.raise();
                        break;
                    case GRTJoystick.KEY_BUTTON_2:
                        climber.lower();
                        break;
                    case GRTJoystick.KEY_BUTTON_9:
                        GRTConstants.updateConstants();
                        break;
                }
            } else if (e.getSource() == secondary) {
                switch (e.getButtonID()) {
                    case GRTXboxJoystick.KEY_BUTTON_X:
                        shooter.setAngle(shooterDown);
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_Y:
                        shooter.setAngle(shooterPresetY);
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_B:
                        shooter.setAngle(shooterPresetB);
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_LEFT_SHOULDER:
                        shooter.setSpeed(shootingSpeed);
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_RIGHT_SHOULDER:
                        if (canShoot) {
                            shooter.shoot();
                            new Thread(shooterTimer).start();
                        }
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_BACK:
                        logInfo("Storing angle.");
                        storedAngle = shooter.getShooterAngle();
                        System.out.println(storedAngle);
                        break;
                    case GRTXboxJoystick.KEY_BUTTON_START:
                        logInfo("Going to stored angle: " + storedAngle);
                        shooter.setAngle(storedAngle);
                }
            }
        } catch (NullPointerException ex) {
            logError("Null pointer encountered when trying to categorize button events");
            ex.printStackTrace();
        }
    }

    public void buttonReleased(ButtonEvent e) {
        if (e.getSource() == leftJoy) {
            switch (e.getButtonID()) {
            }
        } else if (e.getSource() == rightJoy) {
            switch (e.getButtonID()) {
                case GRTJoystick.KEY_BUTTON_3:
                case GRTJoystick.KEY_BUTTON_2:
                    joystickBeltsRunning = false;
                    pickerUpper.stopRoller();
                    pickerUpper.stopRaiser();   //Remove the compression we were placing on the frisbee
                    if(!xBoxBeltsRunning)
                    {
                        belts.stop();
                    }
                    break;
                case GRTJoystick.KEY_BUTTON_4:
                case GRTJoystick.KEY_BUTTON_5:
                    pickerUpper.stopRaiser();
                    break;
            }
        } else if (e.getSource() == secondary) {
            switch (e.getButtonID()) {
                case GRTXboxJoystick.KEY_BUTTON_X:
                    shooter.setFlywheelOutput(0.0);
                    break;
                case GRTXboxJoystick.KEY_BUTTON_A:
                    shooter.setFlywheelOutput(0.0);
                    break;
                case GRTXboxJoystick.KEY_BUTTON_B:
                    shooter.setFlywheelOutput(0.0);
                    break;
                case GRTXboxJoystick.KEY_BUTTON_LEFT_SHOULDER:
                    shooter.setSpeed(0.0);
                    shooter.setFlywheelOutput(0.0);
                    break;
                case GRTXboxJoystick.KEY_BUTTON_RIGHT_SHOULDER:
                    shooter.unShoot();
                    break;
            }
        }
    }

    public void leftXAxisMoved(XboxJoystickEvent e) {
    }

    public void leftYAxisMoved(XboxJoystickEvent e) {
        double scaleFactor = -3.0;
        if (e.getSource() == secondary) {
            System.out.println("adjusting shooter by " + e.getData() / scaleFactor);
            shooter.adjustHeight(e.getData() / scaleFactor);
        }
    }

    public void leftAngleChanged(XboxJoystickEvent e) {
    }

    public void rightXAxisMoved(XboxJoystickEvent e) {
    }

    public void rightYAxisMoved(XboxJoystickEvent e) {
        if (e.getSource() == secondary) {
            shooter.adjustHeight(e.getData());
        }
    }

    public void padMoved(XboxJoystickEvent e) {
        int oldPadPosition = padPosition;
        if (oldPadPosition != (padPosition = e.getData() > 0.5 ? 1 : (e.getData() < -0.5 ? -1 : 0))) {
            shooter.incrementAngle(padPosition * DEG_INCREMENT);
        }
    }

    public void triggerMoved(XboxJoystickEvent e) {
        if (e.getSource() == secondary) {
            if (Math.abs(e.getData()) <= 0.1) {
                xBoxBeltsRunning = false;
                if(!joystickBeltsRunning)
                {
                    belts.stop();
                }
            } else if (e.getData() > 0.0) {
                xBoxBeltsRunning = true;
                belts.moveDown();
            } else {
                xBoxBeltsRunning = true;
                belts.moveUp();
            }
        }
    }

    public void valueChanged(PotentiometerEvent e) {
        System.out.println("potentiometer value changed: " + e.getData());
    }

    public final void updateConstants() {
        try {
            turningDivider = GRTConstants.getValue("turningDivider");
        } catch (Exception e) {
            turningDivider = 2.0;
        }

        shooterPresetB = GRTConstants.getValue("anglePyramidFrontPreset");
        shooterPresetY = GRTConstants.getValue("anglePyramidBackCenter");
        shooterDown = GRTConstants.getValue("shooterDown");
        shootingSpeed = GRTConstants.getValue("shootingRPMS");
    }
    private Runnable shooterTimer = new Runnable() {
        public void run() {
            canShoot = false;
            Timer.delay(shotDelay);
            canShoot = true;
        }
    };
}
