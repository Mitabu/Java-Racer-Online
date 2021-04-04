// Java
import java.io.*;
import java.util.*;
import java.net.*;

/**
*  @author Artem Polkovnikov
*  @version 31.03.2021
*/

public class Opponent implements Serializable
{
   // Attributes
   private int clientNumber;
   private String clientName;
   private String carFileName;
   private double startX;
   private double startY;
   private double startDegree;
   
   public Opponent(int _clientNumber, String _clientName, String _carFileName, double _sx, double _sy, double _sd)
   {
      this.clientNumber = _clientNumber;
      this.clientName = _clientName;
      this.carFileName = _carFileName;
      this.startX = _sx;
      this.startY = _sy;
      this.startDegree = _sd;
   }
   
   public int getClientNumber() {return this.clientNumber;}
   public String getClientName() {return this.clientName;}
   public String getCarFileName() {return this.carFileName;}
   public double getStartX() {return this.startX;}
   public double getStartY() {return this.startY;}
   public double getStartDegree() {return this.startDegree;}
   
   public void setCarFileName(String _carFileName) {this.carFileName = _carFileName;}
   public void setStartX(double _startX) {this.startX = _startX;}
   public void setStartY(double _startY) {this.startY = _startY;}
   public void setStartDegree(double _startDegree) {this.startDegree = _startDegree;}

   public String toString()
   {
      return String.format("OpponentClientNum: %d  CarFileName: %s  X: %f  Y: %f  Degree: %f", this.clientNumber, this.carFileName, this.startX, this.startY, this.startDegree);
   }
}