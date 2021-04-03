import java.io.*;

/**
*  @author Artem Polkovnikov
*  @version 03.04.2021
*/

/** Stores a position on a cartisian plane as a set of X and Y coordinates*/
public class CheckPoint implements Serializable
{              
    // Members
    private double x1;
    private double y1;
    
    private double x2;
    private double y2;
    
    // Accessors
    public double getX1() {return this.x1;}
    public double getY1() {return this.y1;}
    public double getX2() {return this.x2;}
    public double getY2() {return this.y2;}
    // Mutators
    public void setX1(double _x) {this.x1 = _x;}
    public void setY1(double _y) {this.y1 = _y;}
    public void setX2(double _x) {this.x2 = _x;}
    public void setY2(double _y) {this.y2 = _y;}
       
    /** 
    * Constructor
    * @param _x the X-axis coordinate
    * @param _y the Y-axis coordinate
    */        
    public CheckPoint(double _x, double _y, double _x2, double _y2)
    {
        this.x1 = _x;
        this.y1 = _y;
        this.x2 = _x2;
        this.y2 = _y2;
    }
    
    /** Returns coordinates of THIS position*/
    public String toString()
    {
      return String.format("Point 1: %f, %f      Point 2: %f, %f", this.x1, this.y1, this.x2, this.y2);
    }
}