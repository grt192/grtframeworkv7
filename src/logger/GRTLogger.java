package logger;

import com.sun.squawk.microedition.io.FileConnection;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Timer;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 * Static class that is responsible for all system logging.
 *
 * @author agd
 */
public final class GRTLogger {

    private GRTLogger() {
    }
    private static final DriverStationLCD dash =
            DriverStationLCD.getInstance();
    private static final int LOGTYPE_INFO = 0;
    private static final int LOGTYPE_ERROR = 1;
    private static final int LOGTYPE_SUCCESS = 2;
    //Prefixes for the three kinds of log messages
    private static final String[] PREFIX = {"[INFO]:", "[ERROR]:", "[SUCCESS]:"};
    private static final Vector dsBuffer = new Vector();
    private static Vector logReceivers = new Vector();
    private static boolean fileLogging = false;
    private static boolean logging     = true;
    private static String loggingFileName;     //Files to which we log our output.
    private static PrintStream fileWriter;

    static {
        for (int i = 0; i < 6; i++) {
            dsBuffer.addElement("");
        }
        try {
            FileConnection numFile = (FileConnection) Connector.open("file:///logs/filenum.log");

            int fileNum;

            if (!numFile.exists()) {
                fileNum = 1;
            } else {
                fileNum = numFile.openInputStream().read();
                numFile.delete();
            }

            String loggingFile = "/logs/log" + fileNum + ".log";
            GRTLogger.setLoggingFile(loggingFile);
            GRTLogger.enableFileLogging();

            fileNum++;

            if (!numFile.exists()) {
                numFile.create();
            }

            numFile.openOutputStream().write(fileNum);

            numFile.close();
        } catch (IOException e) {
            throw new Error("File logging fail");
        }
    }
    
    public static void enableLogging(){
        logging = true;
    }
    
    public static void disableLogging(){
        logging = false;
    }

    /**
     * Enable logging to a file.
     */
    public static void enableFileLogging() {
        fileLogging = true;
    }

    /**
     * Disable logging to a file.
     */
    public static void disableFileLogging() {
        fileLogging = false;
    }

    /**
     * File path to log to.
     *
     * @param filename absolute file path, e.g.: "/081912-001253.txt"
     */
    public static void setLoggingFile(String filename) {
        loggingFileName = filename;

        //if there are previous connections, finish writing and close them all
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Log a general message.
     *
     * @param data message to log.
     */
    public static void logInfo(String data) {
        log(data, LOGTYPE_INFO);
    }

    /**
     * Log an error message.
     *
     * @param data message to log.
     */
    public static void logError(String data) {
        log(data, LOGTYPE_ERROR);
    }

    /**
     * Log a success message.
     *
     * @param data message to log.
     */
    public static void logSuccess(String data) {
        log(data, LOGTYPE_SUCCESS);
    }

    private static void log(String data, int logtype) {
        if (!logging){
            return;
        }
        
        String message = elapsedTime() + " " + PREFIX[logtype] + data;
        System.out.println("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b" + message);

        if (fileLogging) {
            logLineToFile(message);
        }
    }

    /**
     * Logs a general message, and displays it on the driver station.
     *
     * @param data message to log
     */
    public static void dsLogInfo(String data) {
        dsLog(data, LOGTYPE_INFO);
    }

    /**
     * Logs an error message, and displays it on the driver station.
     *
     * @param data message to log
     */
    public static void dsLogError(String data) {
        dsLog(data, LOGTYPE_ERROR);
    }

    /**
     * Logs a success message, and displays it on the driver station.
     *
     * @param data message to log
     */
    public static void dsLogSuccess(String data) {
        dsLog(data, LOGTYPE_SUCCESS);
    }

    private static void dsLog(String data, int logtype) {
        dsPrintln(PREFIX[logtype] + data);
        log(data, logtype);
    }

    private static void logLineToFile(String message) {
        /* 
         * Note: because it only prepends "file://" with 2 slashes,
         * loggingFileNames[fileDescriptor] should return an
         * absolute path (ex: /logging/info_081912-001253.txt)
         */
        String url = "file://" + loggingFileName;

        //if connection and writer not already created, open one
        if (fileWriter == null) {
            try {
                FileConnection conn = (FileConnection) Connector.open(url);
                if (!conn.exists()) {
                    conn.create();
                }
                PrintStream ps = new PrintStream(conn.openOutputStream());
                fileWriter = ps;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //write stuff to file, and flush
        try {
            fileWriter.println(message);
            fileWriter.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String elapsedTime() {
        StringBuffer s = new StringBuffer();

        int secElapsed = (int) Timer.getFPGATimestamp();
        int minElapsed = secElapsed / 60;
        int hrElapsed = minElapsed / 60;

        if (hrElapsed < 10) {
            s.append("0");
        }
        s.append(hrElapsed).append(":");

        if (minElapsed % 60 < 10) {
            s.append("0");
        }
        s.append(minElapsed % 60).append(":");

        if (secElapsed % 60 < 10) {
            s.append("0");
        }
        s.append(secElapsed % 60);

        return s.toString();
    }

    private static void dsPrintln(String data) {
        dsBuffer.addElement(data);
        dsBuffer.removeElementAt(0);

        dash.println(DriverStationLCD.Line.kUser1, 1,
                (String) dsBuffer.elementAt(5));
        dash.println(DriverStationLCD.Line.kUser6, 1,
                (String) dsBuffer.elementAt(4));
        dash.println(DriverStationLCD.Line.kUser5, 1,
                (String) dsBuffer.elementAt(3));
        dash.println(DriverStationLCD.Line.kUser4, 1,
                (String) dsBuffer.elementAt(2));
        dash.println(DriverStationLCD.Line.kUser3, 1,
                (String) dsBuffer.elementAt(1));
        dash.println(DriverStationLCD.Line.kUser2, 1,
                (String) dsBuffer.elementAt(0));

        dash.updateLCD();
    }
}