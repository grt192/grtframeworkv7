package core;

import com.sun.squawk.io.BufferedReader;
import com.sun.squawk.microedition.io.FileConnection;
import event.listeners.ConstantUpdateListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import logger.GRTLogger;

/**
 * Keeps track of constants from files. Constants are stored as a lookup table,
 * with a string id corresponding to a numeric constant Constants file should be
 * formatted as a list, with a single id/constant pair per line. Each line
 * should be formatted as [String ID],[double constant]
 * '#' indicates commented lines.
 * 
 * In order to facilitate easier constant manipulation, constants may be split
 *
 * @author Calvin
 */
public class GRTConstants {

    private static final String FILE_LOC = "file:///constants.txt"; //list of constant file locations
    private static final char DELIMITER = ',';
    private static final Hashtable table = new Hashtable();
    
    private static Vector constantUpdateListeners = new Vector();
   

    static {
        loadConstants();
        GRTLogger.logSuccess("Constants loaded");
    }

    private GRTConstants() {
    }

    /**
     * Reloads constants from the files
     */
    private static void loadConstants() {
        Vector lines = readFile(FILE_LOC);
        
        if (((String) lines.firstElement()).startsWith("file:///")) { //if starts with URI, assume is list of constant files
            for (Enumeration en = lines.elements(); en.hasMoreElements();) {
                String loc;
                if ((loc = (String) en.nextElement()).startsWith("file:///")) {
                    loadConstantsFromFile(loc);
                }
            }
        } else { //if doesn't start with URI, assume is list of constants
            loadConstantsFromFile(FILE_LOC);
        }
    }
    
    /**
     * Reads a file, returns a list of lines (strings) in the file.
     * Skips empty lines and lines starting with '#'.
     * 
     * @param loc URI of the file
     * @return Vector of strings, each string a line in the file
     * @throws IOException 
     */
    private static Vector readFile(String loc) {
        Vector lines = new Vector();
        try {
            FileConnection fc = (FileConnection) Connector.open(loc);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(fc.openInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals("") && !line.startsWith("#")) {
                    lines.addElement(line);
                }
            }

            reader.close();
            fc.close();
        } catch (IOException e) {
            GRTLogger.logError("Constants file IO Error--probably nonexistent: "
                    + e.getMessage());
        }

        return lines;
    }
    
    private static void loadConstantsFromFile(String loc) {
        Vector lines = readFile(loc);
        try {
            for (Enumeration en = lines.elements(); en.hasMoreElements();) {
                String line = (String) en.nextElement();

                int separatorIndex = line.indexOf(DELIMITER);
                String id = line.substring(0, separatorIndex);
                double data =
                        Double.parseDouble(line.substring(separatorIndex + 1));

                table.put(id, new Double(data));
            }
        } catch (NumberFormatException e) {
            GRTLogger.logError("Malformed constants file: "
                    + e.getMessage());
            throw new Error("Malformed constants file");
        } catch (IndexOutOfBoundsException e) {
            GRTLogger.logError("Malformed constants file"
                    + e.getMessage());
            throw new Error("Mailformed constants file");
        }
    }
    
    /**
     * Loads constants, and updates listeners.
     */
    public static void updateConstants() {
        loadConstants();
        
        for (Enumeration en = constantUpdateListeners.elements();
                en.hasMoreElements();)
            ((ConstantUpdateListener) en.nextElement()).updateConstants();
        
        System.out.println("Updated Constants");
    }

    /**
     * Get the constants value for a particular id.
     *
     * @param id String ID for a constant
     * @return the constant associated with this id, or Double.NaN if no such
     * key exists
     */
    public static double getValue(String id) {
        if (!table.containsKey(id)) {
            System.out.println("Constants string \"" + id + "\" not found");
        }
        return ((Double) table.get(id)).doubleValue();
    }
    
    public static void addListener(ConstantUpdateListener l) {
        constantUpdateListeners.addElement(l);
    }
}