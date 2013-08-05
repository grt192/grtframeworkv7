package sensor;

import core.GRTConstants;
import core.Sensor;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.wpilibj.camera.AxisCameraException;
import edu.wpi.first.wpilibj.image.BinaryImage;
import edu.wpi.first.wpilibj.image.ColorImage;
import edu.wpi.first.wpilibj.image.CriteriaCollection;
import edu.wpi.first.wpilibj.image.LinearAverages;
import edu.wpi.first.wpilibj.image.NIVision;
import edu.wpi.first.wpilibj.image.NIVisionException;
import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A polling sensor that updates the state of the vision target's centroid in
 * image (X,Y), as well as the distance to the target.
 * 
 * @author agd
 * 
 */
public class GRTVisionTracker extends Sensor {

    private double targetDistance;
    private int targetDirection; // -1: right 1: left 0: locked on

    
    public static final int KEY_CENTROID_X = 1;
    public static final int KEY_CENTROID_Y = 2;
    public static final int KEY_CENTROID_X_NORMALIZED = 3;
    public static final int KEY_CENTROID_Y_NORMALIZED = 4;
    public static final int KEY_CENTROID_DISTANCE = 5; //Distance to the centroid

    public static final int NUM_DATA = 6;

    private AxisCamera camera;      //The axis camera from which we're receiving frames.

    //Private variables that capture the state of the vision tracker as of the last frame
    private double centroid_x, centroid_y,
            centroid_x_normalized, centroid_y_normalized;
    private double distance;

    private final int XMAXSIZE = 24;
    private final int XMINSIZE = 24;
    private final int YMAXSIZE = 24;
    private final int YMINSIZE = 48;
    private final double xMax[] = {1, 1, 1, 1, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, 1, 1, 1, 1};
    private final double xMin[] = {.4, .6, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, .1, 0.6, 0};
    private final double yMax[] = {1, 1, 1, 1, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, .5, 1, 1, 1, 1};
    private final double yMin[] = {.4, .6, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05,
        .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05, .05,
        .05, .05, .6, 0};

    private final int RECTANGULARITY_LIMIT = 60;
    private final int ASPECT_RATIO_LIMIT = 75;
    private final int X_EDGE_LIMIT = 60;
    private final int Y_EDGE_LIMIT = 40;

    private int X_IMAGE_RES = 320;          //X Image resolution in pixels, should be 160, 320 or 640. Defaults to 320
    //    private final double VIEW_ANGLE = 43.5;       //Axis 206 camera
    private final double VIEW_ANGLE = 47;       //Axis M1011 camera
//horz: 47 ver: 35
    private CriteriaCollection cc;

    private static final int SLEEP_TIME = 14;

    private Vector listeners;   //VisionTrackerListeners

    private class Scores {
        double rectangularity;
        double aspectRatioInner;
        double aspectRatioOuter;
        double xEdge;
        double yEdge;
    }


    public GRTVisionTracker(AxisCamera cam) {
        super("Vision Tracker", NUM_DATA);
        this.camera = cam;

        this.cc = new CriteriaCollection();      // create the criteria for the particle filter
        cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_AREA, 500, 65535, false);
        X_IMAGE_RES = camera.getResolution().width;

        listeners = new Vector();
    }

    protected void poll() {
        //Update our image state with a new frame.
        updateImages();

        //Now set our state variables.
        setState(KEY_CENTROID_X, centroid_x);
        setState(KEY_CENTROID_Y, centroid_y);
        setState(KEY_CENTROID_X_NORMALIZED, centroid_x_normalized);
        setState(KEY_CENTROID_Y_NORMALIZED, centroid_y_normalized);
        setState(KEY_CENTROID_DISTANCE, targetDistance);
    }
    
    /**
     * Because the GRTVisionTracker is a special kind of sensor (i.e. one that 
     * we poll sparingly and on demand to keep CPU usage low) we expose a public
     * method that is analogous to its poll() method, that we can call in the
     * context of a macro or otherwise to use the latest frame to gather target
     * information.
     */
    public void updateTrackingData(){
        poll();
    }
    
    /**
     * Get Normalized X coordinate of the target centroid.
     * @return The X coordinate of the center of mass. [-1,1]
     */
    public double getCentroidXNormalized(){
        return getState(KEY_CENTROID_X_NORMALIZED);
    }
    
    /**
     * Get Normalized Y coordinate of the target centroid.
     * @return The Y coordinate of the center of mass. [-1,1]
     */
    public double getCentroidYNormalized(){
        return getState(KEY_CENTROID_Y_NORMALIZED);
    }
    
    /**
     * Return distance from the camera to the centroid.
     * @return The distance in inches from the camera to the centroid.
     */
    public double getDistance(){
        return getState(KEY_CENTROID_DISTANCE);
    }
    
    //Required Sensor.notifyListeners method. Because all access will be done 
    //through public methods, we will leave this unimplemented.
    protected void notifyListeners(int id, double newDatum) {
        //Unimplemented here.
    }

    private void updateImages() {
        logInfo("Updating!");
        try {
            /**
             * Do the image capture with the camera and apply the algorithm
             * described above. This sample will either get images from the
             * camera or from an image file stored in the top level directory in
             * the flash memory on the cRIO. The file name in this case is
             * "testImage.jpg"
             *
             */
            ColorImage image;
            image = camera.getImage();
            image.colorEqualize();
            image.write("/equalized.bmp");

            //image = new RGBImage("/testImage.jpg");		// get the sample image from the cRIO flash
            BinaryImage thresholdImage = image.thresholdHSV((int) GRTConstants.getValue("hlow"),
                    (int) GRTConstants.getValue("hhigh"),
                    (int) GRTConstants.getValue("slow"),
                    (int) GRTConstants.getValue("shigh"),
                    (int) GRTConstants.getValue("vlow"),
                    (int) GRTConstants.getValue("vhigh"));   // keep only green objects
            BinaryImage convexHullImage = thresholdImage.convexHull(false);          // fill in occluded rectangles
            BinaryImage filteredImage = convexHullImage.particleFilter(cc);           // filter out small particles
    
            //iterate through each particle and score to see if it is a target
            GRTVisionTracker.Scores scores[] = new GRTVisionTracker.Scores[filteredImage.getNumberParticles()];
            int targetIndex = 0;
            double targetIndexArea = 0;
            
            for (int i = 0; i < scores.length; i++) {
                ParticleAnalysisReport report = filteredImage.getParticleAnalysisReport(i);
               
                scores[i] = new GRTVisionTracker.Scores();

                scores[i].rectangularity = scoreRectangularity(report);
                scores[i].aspectRatioOuter = scoreAspectRatio(filteredImage, report, i, true);
                scores[i].aspectRatioInner = scoreAspectRatio(filteredImage, report, i, false);
                scores[i].xEdge = scoreXEdge(thresholdImage, report);
                scores[i].yEdge = scoreYEdge(thresholdImage, report);
                
                if (scoreCompare(scores[i], false) || (report.boundingRectWidth/report.boundingRectHeight > (320/175 - 0.5) && report.boundingRectWidth/report.boundingRectHeight < (320/175 + 0.5)) ) {
//                    centroid_x = report.center_mass_x;
//                    centroid_y = report.center_mass_y;
//
//                    centroid_x_normalized = report.center_mass_x_normalized;
//                    centroid_y_normalized = report.center_mass_y_normalized;
//
//                    distance = computeDistance(thresholdImage, report, i, true);
//                    //                    System.out.println("particle: " + i + "is a High Goal  centerX: " + report.center_mass_x_normalized + "centerY: " + report.center_mass_y_normalized);
//                    //                    System.out.println("Distance: " + computeDistance(thresholdImage, report, i, false));
//
//                    System.out.println("Distance: " + computeDistance(thresholdImage, report, i, true));
                    
                if(scores[i].xEdge * scores[i].yEdge >= targetIndexArea){
                    targetIndex = i;
                    targetIndexArea = scores[i].xEdge * scores[i].yEdge;
                }
                                  
                    
                } 

                
                   /*else if (scoreCompare(scores[i], true)) {
                   System.out.println("particle: " + i + "is a Middle Goal  centerX: " + report.center_mass_x_normalized + "centerY: " + report.center_mass_y_normalized);
                   System.out.println("Distance: " + computeDistance(thresholdImage, report, i, true));
                   }
                  */

                else {
                    //logError("particle: " + i + " is not a goal\tcenterX: " + report.center_mass_x_normalized + "\tcenterY: " + report.center_mass_y_normalized);
                }
                //                System.out.println("rect: " + scores[i].rectangularity + "ARinner: " + scores[i].aspectRatioInner);
                //                System.out.println("ARouter: " + scores[i].aspectRatioOuter + "xEdge: " + scores[i].xEdge + "yEdge: " + scores[i].yEdge);
            }
            
            if (targetIndex == -1){
                return;
            }
            ParticleAnalysisReport targetReport = filteredImage.getParticleAnalysisReport(targetIndex);
            targetIndexArea = 0;
            targetDistance = computeDistance(filteredImage, targetReport, targetIndex, false);
    
            /*trial 1: 
             * 66 * 42
             * code: 174.23800427863011
             * actual: 211
             * 
             * trial 2:
             * 77*55
             * code: 145.56592762518466
             * actual: 164
             
             * trial 3:
             * 84*54
             * code: 153
             * actual: 164

            /**
             * all images in Java must be freed after they are used since they
             * are allocated out of C data structures. Not calling free() will
             * cause the memory to accumulate over each pass of this loop.
             */
            filteredImage.free();
            convexHullImage.free();
            thresholdImage.free();
            image.free();
      
            
        } catch (AxisCameraException ex) {
            ex.printStackTrace();
        } catch (NIVisionException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Computes the estimated distance to a target using the height of the particle in the image. For more information and graphics
     * showing the math behind this approach see the Vision Processing section of the ScreenStepsLive documentation.
     * 
     * @param image The image to use for measuring the particle estimated rectangle
     * @param report The Particle Analysis Report for the particle
     * @param outer True if the particle should be treated as an outer target, false to treat it as a center target
     * @return The estimated distance to the target in Inches.
     */
    double computeDistance (BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean outer) throws NIVisionException {
        double rectShort, width, height;
        double targetWidth, targetHeight;
        

        rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
        //using the smaller of the estimated rectangle short side and the bounding rectangle height results in better performance
        //on skewed rectangles
        //height = Math.min(report.boundingRectHeight, rectShort);
        width = report.boundingRectWidth;
        height = report.boundingRectHeight;
        //targetHeight = outer ? 29 : 21;
        //changed by Yonatan Oren//
        //need to change this back to 29/21 for for real ultimate ascent//
        targetWidth = 16;
        targetHeight = 9.75;
        
////
//         System.out.println("rectShort: " + rectShort);
//         System.out.println("height: " + height);
//         System.out.println("boundingRectHeight: " + report.boundingRectHeight);

         //changed by Yonatan Oren//
        //return X_IMAGE_RES * targetHeight / (height * 12 * 2 * Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
         //return 240.0 * targetWidth / (width * Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
         return 360.0 * targetHeight / (height * Math.tan(VIEW_ANGLE*Math.PI/(180*2)));
         //4800 / 62 * tan(
    }

    /**
     * Computes a score (0-100) comparing the aspect ratio to the ideal aspect ratio for the target. This method uses
     * the equivalent rectangle sides to determine aspect ratio as it performs better as the target gets skewed by moving
     * to the left or right. The equivalent rectangle is the rectangle with sides x and y where particle area= x*y
     * and particle perimeter= 2x+2y
     * 
     * @param image The image containing the particle to score, needed to performa additional measurements
     * @param report The Particle Analysis Report for the particle, used for the width, height, and particle number
     * @param outer	Indicates whether the particle aspect ratio should be compared to the ratio for the inner target or the outer
     * @return The aspect ratio score (0-100)
     */
    public double scoreAspectRatio(BinaryImage image, ParticleAnalysisReport report, int particleNumber, boolean outer) throws NIVisionException
    {
        double rectLong, rectShort, aspectRatio, idealAspectRatio;

        rectLong = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_LONG_SIDE);
        rectShort = NIVision.MeasureParticle(image.image, particleNumber, false, NIVision.MeasurementType.IMAQ_MT_EQUIVALENT_RECT_SHORT_SIDE);
        //idealAspectRatio = outer ? (62/29) : (62/20);	//Dimensions of goal opening + 4 inches on all 4 sides for reflective tape

        //yonatan - change back
        idealAspectRatio = outer ? (62/29) : (62/40);	//Dimensions of goal opening + 4 inches on all 4 sides for reflective tape

        //Divide width by height to measure aspect ratio
        if(report.boundingRectWidth > report.boundingRectHeight){
            //particle is wider than it is tall, divide long by short
            aspectRatio = 100*(1-Math.abs((1-((rectLong/rectShort)/idealAspectRatio))));
        } else {
            //particle is taller than it is wide, divide short by long
            aspectRatio = 100*(1-Math.abs((1-((rectShort/rectLong)/idealAspectRatio))));
        }
        return (Math.max(0, Math.min(aspectRatio, 100.0)));		//force to be in range 0-100
    }

    /**
     * Compares scores to defined limits and returns true if the particle appears to be a target
     * 
     * @param scores The structure containing the scores to compare
     * @param outer True if the particle should be treated as an outer target, false to treat it as a center target
     * 
     * @return True if the particle meets all limits, false otherwise
     */
    boolean scoreCompare(GRTVisionTracker.Scores scores, boolean outer){
        boolean isTarget = true;

        isTarget &= scores.rectangularity > RECTANGULARITY_LIMIT;

        
           if(outer){
           isTarget &= scores.aspectRatioOuter > ASPECT_RATIO_LIMIT;
//           if(isTarget == false){System.out.println("yonatan: aspectRatioOuter !> Aspect Ratio Limit");}
           } else {
           isTarget &= scores.aspectRatioInner > ASPECT_RATIO_LIMIT;
//           if(isTarget == false){System.out.println("yonatan: scores.aspectRatioInner > ASPECT_RATIO_LIMIT");}
         }
           isTarget &= scores.xEdge > X_EDGE_LIMIT;
//            if(isTarget == false){System.out.println("yonatan: scores.xEdge > X_EDGE_LIMIT");}
           isTarget &= scores.yEdge > Y_EDGE_LIMIT;
//            if(isTarget == false){System.out.println("yonatan: scores.yEdge > Y_EDGE_LIMIT;");}
          
        return isTarget;
    }

    /**
     * Computes a score (0-100) estimating how rectangular the particle is by comparing the area of the particle
     * to the area of the bounding box surrounding it. A perfect rectangle would cover the entire bounding box.
     * 
     * @param report The Particle Analysis Report for the particle to score
     * @return The rectangularity score (0-100)
     */
    double scoreRectangularity(ParticleAnalysisReport report){
        if(report.boundingRectWidth*report.boundingRectHeight !=0){
            return 100*report.particleArea/(report.boundingRectWidth*report.boundingRectHeight);
        } else {
            return 0;
        }	
    }

    /**
     * Computes a score based on the match between a template profile and the particle profile in the X direction. This method uses the
     * the column averages and the profile defined at the top of the sample to look for the solid vertical edges with
     * a hollow center.
     * 
     * @param image The image to use, should be the image before the convex hull is performed
     * @param report The Particle Analysis Report for the particle
     * 
     * @return The X Edge Score (0-100)
     */
    public double scoreXEdge(BinaryImage image, ParticleAnalysisReport report) throws NIVisionException
    {
        double total = 0;
        LinearAverages averages;

        NIVision.Rect rect = new NIVision.Rect(report.boundingRectTop, report.boundingRectLeft, report.boundingRectHeight, report.boundingRectWidth);
        averages = NIVision.getLinearAverages(image.image, LinearAverages.LinearAveragesMode.IMAQ_COLUMN_AVERAGES, rect);
        float columnAverages[] = averages.getColumnAverages();
        for(int i=0; i < (columnAverages.length); i++){
            if(xMin[(i*(XMINSIZE-1)/columnAverages.length)] < columnAverages[i] 
                    && columnAverages[i] < xMax[i*(XMAXSIZE-1)/columnAverages.length]){
                total++;
                    }
        }
        total = 100*total/(columnAverages.length);
        return total;
    }

    /**
     * Computes a score based on the match between a template profile and the particle profile in the Y direction. This method uses the
     * the row averages and the profile defined at the top of the sample to look for the solid horizontal edges with
     * a hollow center
     * 
     * @param image The image to use, should be the image before the convex hull is performed
     * @param report The Particle Analysis Report for the particle
     * 
     * @return The Y Edge score (0-100)
     *
     */
    public double scoreYEdge(BinaryImage image, ParticleAnalysisReport report) throws NIVisionException
    {
        double total = 0;
        LinearAverages averages;

        NIVision.Rect rect = new NIVision.Rect(report.boundingRectTop, report.boundingRectLeft, report.boundingRectHeight, report.boundingRectWidth);
        averages = NIVision.getLinearAverages(image.image, LinearAverages.LinearAveragesMode.IMAQ_ROW_AVERAGES, rect);
        float rowAverages[] = averages.getRowAverages();
        for(int i=0; i < (rowAverages.length); i++){
            if(yMin[(i*(YMINSIZE-1)/rowAverages.length)] < rowAverages[i] 
                    && rowAverages[i] < yMax[i*(YMAXSIZE-1)/rowAverages.length]){
                total++;
                    }
        }
        total = 100*total/(rowAverages.length);
        return total;
    }

}
