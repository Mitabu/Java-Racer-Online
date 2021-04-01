// Java
import java.io.*;
import java.util.*;
import java.net.*;

/**
*  @author Artem Polkovnikov
*  @version 31.03.2021
*/

public class CoordinateSet implements Serializable
{
   // Attributes
   private int clientNumber;
   private double x;
   private double y;
   private double degree;
   
   public CoordinateSet(int _clientNumber, double _sx, double _sy, double _sd)
   {
      this.clientNumber = _clientNumber;
      this.x = _sx;
      this.y = _sy;
      this.degree = _sd;
   }
   
   public int getClientNumber() {return this.clientNumber;}
   public double getX() {return this.x;}
   public double getY() {return this.y;}
   public double getDegree() {return this.degree;}
   
   public void setX(double _x) {this.x = _x;}
   public void setY(double _y) {this.y = _y;}
   public void setDegree(double _degree) {this.degree = _degree;}
}