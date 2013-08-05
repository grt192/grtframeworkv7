package event.listeners;

import event.events.MacroEvent;

/**
 *
 * @author keshav
 */
public interface MacroListener {

    public void macroInitialized(MacroEvent e);

    public void macroDone(MacroEvent e);
    
    public void macroTimedOut(MacroEvent e);

}
