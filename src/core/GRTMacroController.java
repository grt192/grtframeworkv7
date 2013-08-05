package core;

import event.events.MacroEvent;
import event.listeners.MacroListener;
import java.util.Enumeration;
import java.util.Vector;
import logger.GRTLogger;

/**
 * Creates a new MacroController that executes macros in sequence.
 *
 * Macros can also be executed concurrently with others, if added
 * to the concurrentMacros vector.
 * 
 * @author keshav, calvin
 */
public class GRTMacroController extends EventController implements MacroListener {

    private Vector macros;
    private int currentIndex = -1;


    /**
     * Constructor that creates an empty list
     * of macros.
     */
    public GRTMacroController(){
        this(new Vector());
    }
    
    /**
     * Creates a new GRTMacroController.
     * @param macros list of macros to run
     */
    public GRTMacroController(Vector macros) {
        super("Macro controller");
        this.macros = macros;
    }

    protected void startListening() {
//        System.out.println("Number of macros: " + macros.size());

        System.out.println("start listen");
        currentIndex = -1;
        for (Enumeration en = macros.elements(); en.hasMoreElements();) {
            GRTMacro m = (GRTMacro) en.nextElement();
            m.reset();
            m.addListener(this);
        }
        
        startNextMacro();
    }
    
    public void addMacro(GRTMacro m){
        System.out.println("GRTMacroController " + getID() + "  adding macro #" + (macros.size() + 1));
        macros.addElement(m);
    }

    protected void stopListening() {
        System.out.println("Disabling macrocontroller");
        for (Enumeration en = macros.elements(); en.hasMoreElements();) {
            GRTMacro m = (GRTMacro) en.nextElement();
            m.removeListener(this);    
            m.kill();
        }
    }   

    public void macroInitialized(MacroEvent e) {
        GRTLogger.logInfo("Initialized macro: " + e.getSource().getID());
    }

    public void macroDone(MacroEvent e) {
        GRTLogger.logInfo("Completed macro: " + e.getSource().getID());
            startNextMacro();
    }

    public void macroTimedOut(MacroEvent e) {
        GRTLogger.logError("Macro " + e.getSource().getID() +
                " timed out. Skipping macros.");
    }
    
    private void startNextMacro() {
        System.out.println("Next macro up to bat!");
        if (++currentIndex < macros.size()) {
            System.out.println("Starting new Macros!");
            GRTMacro macro = (GRTMacro) macros.elementAt(currentIndex);
            System.out.println("\tIt's a regular macro! " + macro.getID());
            macro.execute();
        } else {
            GRTLogger.logSuccess("Completed all macros. Waiting for teleop!");
        }
    }
}
