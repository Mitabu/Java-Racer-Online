/**
*  @author Artem Polkovnikov
*  @version 15.03.2021
*/

/** Stores a position as a set of X and Y coordinates*/
public class Position
{              
    // Members
    /** X coordinate*/
    private double x;
    /** Y coordinate*/
    private double y;
    
    // Accessors
    public double getX() {return this.x;}
    public double getY() {return this.y;}
    
    // Mutators
    public void setX(double _x) {this.x = _x;}
    public void setY(double _y) {this.y = _y;}
       
    /** 
    * Constructor that sets up a position
    *
    * @param _x the X-axis coordinate
    * @param _y the Y-axis coordinate
    */        
    public Position(double _x, double _y)
    {
        this.x = _x;
        this.y = _y;
    }
    
    /**
    * Returns formatted coordinates of the position
    *
    * @return formatted coordinates of the position
    */
    public String toString()
    {
      return "x: " + this.x + " y: " + this.y;
    }
}