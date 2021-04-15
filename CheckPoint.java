// Java
import java.io.*;

/**
*  @author Artem Polkovnikov
*  @version 03.04.2021
*/

/** Stores a position as a set of X and Y coordinates*/
public class CheckPoint implements Serializable
{              
    // Members
    /** First point X coordinate*/
    private double x1;
    /** First point Y coordinate*/
    private double y1;
    
    /** Second point X coordinate*/
    private double x2;
    /** Second point Y coordinate*/
    private double y2;
    
    // Accessors
    /** 
    * First point X coordinate accessor
    * @return first point X coordinate
    */
    public double getX1() {return this.x1;}
    /** 
    * First point Y coordinate accessor
    * @return first point Y coordinate
    */
    public double getY1() {return this.y1;}
    /** 
    * Second point X coordinate accessor
    * @return second point X coordinate
    */
    public double getX2() {return this.x2;}
    /** 
    * Second point Y coordinate accessor
    * @return second point Y coordinate
    */
    public double getY2() {return this.y2;}
    
    // Mutators
    /**
    * First point X coordinate mutator
    * @param _x First point X coordinate
    */
    public void setX1(double _x) {this.x1 = _x;}
    /**
    * First point Y coordinate mutator
    * @param _y First point Y coordinate
    */
    public void setY1(double _y) {this.y1 = _y;}
    /**
    * Second point X coordinate mutator
    * @param _x First point X coordinate
    */
    public void setX2(double _x) {this.x2 = _x;}
    /**
    * Second point Y coordinate mutator
    * @param _y First point Y coordinate
    */
    public void setY2(double _y) {this.y2 = _y;}
       
    /** 
    * Constructor that sets up the initial coordinates of the check point
    *
    * @param _x first point X-axis coordinate
    * @param _y first point Y-axis coordinate
    * @param _x2 second point X-axis coordinate
    * @param _y2 second point Y-axis coordinate
    */        
    public CheckPoint(double _x, double _y, double _x2, double _y2)
    {
        this.x1 = _x;
        this.y1 = _y;
        this.x2 = _x2;
        this.y2 = _y2;
    }
    
    /** Returns coordinates of the check point as a formatted string*/
    public String toString()
    {
      return String.format("Point 1: %f, %f      Point 2: %f, %f", this.x1, this.y1, this.x2, this.y2);
    }
}