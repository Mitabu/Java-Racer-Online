/**
*  @author Artem Polkovnikov
*  @version 15.03.2021
*/

/** Creates and performs vector operations on a coordinte set in 2D*/
public class Vector2
{
    // Members
    /** X coordinate of the vector*/
    private double x;
    /** Y coordinate of the vector*/
    private double y;
    
    // Accessors
    public double getX() {return this.x;};
    public double getY() {return this.y;}
    // Mutators
    public void setX(double _x) {this.x = _x;}
    public void setY(double _y) {this.y = _y;}
       
    /** 
    * Standard constructor
    * (creates a vector that points to -1 on Y-AXIS)
    */
    public Vector2()
    {
        this.x = 0.0;
        this.y = -1.0;
    }
    
    /**
    * Creates a Vector2 with set X and Y coordinates
    *
    * @param _x X coordinate of the vector
    * @param _y Y coordinate of the vector
    */
    public Vector2(double _x, double _y)
    {
        this.x = _x;
        this.y = _y;
    }
    
    /**
    * Copies an existing vector into a new vector
    *
    * @param _other an existing vector you want to create a copy of
    */
    public Vector2(Vector2 _other)
    {
        this.x = _other.x;
        this.y = _other.y;
    }
    
    /** 
    * Finds the length of the vector and returns it
    *
    * @return vector length
    */
    public double getLength()
    {
      double length = Math.sqrt( Math.pow(this.x , 2) + Math.pow(this.y , 2));
      return length;
    }
    
    /**
    * Multiplies a vector by a value
    *
    * @param _c the value the vector is multiplied by
    *
    * @return this vector
    */
    public Vector2 dot(double _c) // Multiplication of vectors without changing direciton
    {
      this.x *= _c;
      this.y *= _c;
      
      return this;
    }
    
    /**
    * Adds another vector to THIS vector
    *
    * @param _other the vector that you want to add to this vector
    *
    * @return this vector
    */
    public Vector2 add(Vector2 _other) // Vector addition
    {
      this.x += _other.x;
      this.y += _other.y;
      
      return this;
    }
    
    /**
    * Subtracts another vector from THIS vector
    *
    * @param _other the vector that you want to subtract from this vector
    *
    * @return this vector
    */
    public Vector2 subtract(Vector2 _other) // Vector subtraction
    {
      this.x -= _other.x;
      this.y -= _other.y;
      
      return this;
    }
    
    /**
    * Rotates THIS vector by a number of DEGREES
    *
    * @param _newAngleInDegrees the angle by which you want to rotate THIS vector
    *
    * @return this vector
    */
    public Vector2 rotatedBy(double _newAngleInDegrees) // Anti-clock wise rotation
    {         
      double lengthOfVector = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
      
      double currentAngle = Math.toDegrees(Math.asin(this.y / lengthOfVector));
      
      double newAngle = currentAngle + _newAngleInDegrees;
      
      double newAngleInRadians = Math.toRadians(newAngle);
      
      this.x = lengthOfVector * Math.cos(newAngleInRadians);
      this.y = lengthOfVector * Math.sin(newAngleInRadians);
      
      // Set coordinates to 0 if they are too insignificantly small
      if(this.x < 0.01 && this.x > -0.01)
      {
         this.x = 0;
      }
      if(this.y < 0.01 && this.y > -0.01)
      {
         this.y = 0;
      }
      
      return this;
    }
    
    /** 
    * Returns the rotation of this vector
    *
    * @return rotation of this vector
    */
    public double getRotation()
    {
      double t = (this.y / Math.abs(this.y)) * Math.acos( ( (this.x * 1) + (this.y * 0) ) / (Math.sqrt( (Math.pow(this.x, 2) + Math.pow(this.y, 2) * (1 + 0) ) )) );
      
      return Math.toDegrees(t);
    }
    
    /** 
    * Displays formatted coordinates of this vector
    *
    * @return formatted coordinates of this vector
    */
    public String toString()
    {
      return "x: " + this.x + " y: " + this.y;
    }
}