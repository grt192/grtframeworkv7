/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.sun.squawk.util.MathUtils;
import core.GRTConstants;

/**
 *
 * @author Andrew Duffy <gerberduffy@gmail.com>
 */
public class DeadReckoner {
    private static double x = 0;
    private static double y = 0;
    private static double theta = 0;
    
    private DeadReckoner() {}
    
    private static double degreesToRadians(double theta) {
        return theta * Math.PI / 180.0;
    }
    
    /**
     * Sets the position
     * @param x
     * @param y
     * @param theta
     */
    public static void setPosition(double x, double y, double theta) {
        DeadReckoner.x = x;
        DeadReckoner.y = y;
        DeadReckoner.theta = theta;
    }
    
    /**
     * Notifies distance traveled.
     * @param d distance traveled in meters
     */
    public static void notifyDrive(double d) {
        x += d * Math.sin(degreesToRadians(theta));
        y += d * Math.cos(degreesToRadians(theta));
    }
    
    /**
     * Notifies a turn.
     * @param theta angle turned clockwise, in degrees
     */
    public static void notifyTurn(double theta) {
        DeadReckoner.theta += theta;
    }
    
    public static double distanceFrom(double targetX, double targetY) {
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
    
    public static double angleFrom(double targetX, double targetY) {

        double deltaX = targetX - x;
        double deltaY = targetY - y;

        double targetTheta = MathUtils.atan2(deltaX, deltaY); //flipped x and y because we are using heading
        return targetTheta - theta;
    }
    
    public static double turnAngle(double targetTheta) {
        return targetTheta - theta;
    }
    
    public static double getX() {
        return x;
    }
    
    public static double getY() {
        return y;
    }
    
    public static double getTheta() {
        return theta;
    }
}
