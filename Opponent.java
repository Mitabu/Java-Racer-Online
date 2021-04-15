// Java
import java.io.*;
import java.util.*;
import java.net.*;

/**
*  @author Artem Polkovnikov
*  @version 31.03.2021
*/

/** Stores information about opponent players and allows for it's transfer over a socket*/
public class Opponent implements Serializable
{
   // Attributes
   /** Opponent's identification number*/
   private int clientNumber;
   /** Opponent's nickname*/
   private String clientName;
   /** Opponent's car file name*/
   private String carFileName;
   /** Starting position X coordinate*/
   private double startX;
   /** Starting position Y coordinate*/
   private double startY;
   /** Rotation of the car on the starting position*/
   private double startDegree;
   
   /**
   * Constructor that sets up all of the opponent's parameters
   *
   * @param _clientNumber opponent identification number
   * @param _clientName opponent's nickname
   * @param _carFileName opponent's car file name
   * @param _sx starting x coordinate
   * @param _sy starting y coordinate
   * @param _sd starting rotation in degrees
   */
   public Opponent(int _clientNumber, String _clientName, String _carFileName, double _sx, double _sy, double _sd)
   {
      this.clientNumber = _clientNumber;
      this.clientName = _clientName;
      this.carFileName = _carFileName;
      this.startX = _sx;
      this.startY = _sy;
      this.startDegree = _sd;
   }
   
   // Mutators
   public int getClientNumber() {return this.clientNumber;}
   public String getClientName() {return this.clientName;}
   public String getCarFileName() {return this.carFileName;}
   public double getStartX() {return this.startX;}
   public double getStartY() {return this.startY;}
   public double getStartDegree() {return this.startDegree;}
   
   // Accessors
   public void setCarFileName(String _carFileName) {this.carFileName = _carFileName;}
   public void setStartX(double _startX) {this.startX = _startX;}
   public void setStartY(double _startY) {this.startY = _startY;}
   public void setStartDegree(double _startDegree) {this.startDegree = _startDegree;}
   
   /** 
   * Returns formatted opponent information
   *
   * @return formatted opponent information
   */
   public String toString()
   {
      return String.format("OpponentClientNum: %d  CarFileName: %s  X: %f  Y: %f  Degree: %f", this.clientNumber, this.carFileName, this.startX, this.startY, this.startDegree);
   }
}