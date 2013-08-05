package macro;

import core.GRTMacro;
import sensor.GRTVisionTracker;

/**
 * A macro whose sole purpose is to update the tracking data based on the 
 * most recent frame supplied by the camera.
 * 
 * @author Andrew Duffy <gerberduffy@gmail.com>
 */
public class MacroUpdateTracking extends GRTMacro {

    private GRTVisionTracker vt;
    
    public MacroUpdateTracking(GRTVisionTracker visionTracker){
        super("Macro--Vision Tracker Updating", 1000, 1000);    //Having pollTime == timeout means we will only update once.
        this.vt = visionTracker;
    }

    /**
     * Initialize the macro
     */
    protected void initialize() {
        //No initialization necessary
    }

    /**
     * This macro's sole purpose is to update the tracking information.
     */
    protected void perform() {
        vt.updateTrackingData();
        notifyFinished();
    }

    /**
     * Kill the macro
     */
    protected void die() {
        //I regret that I have but one life to give for my robot.
    }
    
}
