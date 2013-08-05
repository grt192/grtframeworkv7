package event.events;

import core.GRTMacro;


/**
 *
 * @author keshav
 */
public class MacroEvent {

    private GRTMacro source;
    
    public MacroEvent(GRTMacro source){
        this.source = source;
    }
    
    public GRTMacro getSource(){
        return source;
    }
    
}
