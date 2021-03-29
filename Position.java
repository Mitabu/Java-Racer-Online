/**
*  @author Artem Polkovnikov
*  @version 15.03.2021
*/

/** Stores a position on a cortisian plane as a set of X and Y coordinates*/
public class Position
{              
    // Members
    private double x;
    private double y;
    
    // Accessors
    public double getX() {return this.x;}
    public double getY() {return this.y;}
    // Mutators
    public void setX(double _x) {this.x = _x;}
    public void setY(double _y) {this.y = _y;}
       
    /** 
    * Constructor
    * @param _x the X-axis coordinate
    * @param _y the Y-axis coordinate
    */        
    public Position(double _x, double _y)
    {
        this.x = _x;
        this.y = _y;
    }
    
    /** Returns coordinates of THIS position*/
    public String toString()
    {
      return "x: " + this.x + " y: " + this.y;
    }
}