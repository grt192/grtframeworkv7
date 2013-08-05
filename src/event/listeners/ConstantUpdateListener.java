/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package event.listeners;

/**
 * Classes that use GRTConstants may be notified whenever constants are updated,
 * to automatically reload constants from GRTConstants.
 * 
 * @author Calvin
 */
public interface ConstantUpdateListener {
    
    /**
     * Called when GRTConstants is updated, this class will reload constants
     * as necessary.
     */
    public void updateConstants();
    
}
