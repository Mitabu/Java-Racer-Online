// Java
import java.io.*;
import java.util.*;
import java.net.*;

/**
*  @author Artem Polkovnikov
*  @version 31.03.2021
*/

/** Stores coordinates of a client's car for transmission over socket*/
public class CoordinateSet implements Serializable
{
   // Attributes
   /** Identification number of the client*/
   private int clientNumber;
   /** X coordinate of the car*/
   private double x;
   /** Y coordinate of the car*/
   private double y;
   /** Rotation of the car in degrees*/
   private double degree;
   
   /**
   * Constructor that sets up the coordinate set for transmission
   *
   * @param _clientNumber client's identification number
   * @param _sx X coordinate
   * @param _sy Y coordinate
   * @param _sd rotation in degrees
   */
   public CoordinateSet(int _clientNumber, double _sx, double _sy, double _sd)
   {
      this.clientNumber = _clientNumber;
      this.x = _sx;
      this.y = _sy;
      this.degree = _sd;
   }
   
   // Accessors
   public int getClientNumber() {return this.clientNumber;}
   public double getX() {return this.x;}
   public double getY() {return this.y;}
   public double getDegree() {return this.degree;}
   
   // Mutators
   public void setX(double _x) {this.x = _x;}
   public void setY(double _y) {this.y = _y;}
   public void setDegree(double _degree) {this.degree = _degree;}
}