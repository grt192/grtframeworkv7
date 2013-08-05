/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package macro;

import core.GRTMacro;

/**
 *
 * @author keshav
 */
public class MacroDelay extends GRTMacro {
 
    private int timeout = 500;
    
    /**
     * Delay execution for specified time
     * @param delay Time in ms to sleep
     */
    public MacroDelay(int delay) {
        super("Delay Macro", delay + 100);
        this.timeout = delay;
    }

    protected void perform() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        notifyFinished();
    }

    protected void die() {        
    }

    protected void initialize() {
    }

}
