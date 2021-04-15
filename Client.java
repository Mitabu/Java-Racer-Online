// Java
import java.io.*;
import java.util.*;
import java.net.*;

/**
*  @author Artem Polkovnikov
*  @version 04.04.2021
*/

/** Stores client attribues*/
public class Client
{
   // Attributes
   /** Socket assigned to the client*/
   private Socket cSocket = null;
   /** ObjectOutputStream assigned to the client*/
   private ObjectOutputStream oos = null;
   /** ObjectInputStream asssigned to the client*/
   private ObjectInputStream ois = null;
   /** Identification number of the client*/
   private int clientNumber;
   /** Client's nickname*/
   private String clientName;
   /** Name of the car file that the client uses*/
   private String carFileName;
   /** Starting position X coordinate*/
   private double startX;
   /** Starting position Y coordinate*/
   private double startY;
   /** Rotation of the car on the starting position*/
   private double startDegree;
   
   /**
   * Constructor that sets up all of the client's parameters
   *
   * @param _cSocket socket assigned to the client
   * @param _oos ObjectOutputStream assigned to the client
   * @param _ois ObjectInputStream assigned to the client
   * @param _clientNumber client's identification number
   * @param _clientName client's nickname
   */
   public Client(Socket _cSocket, ObjectOutputStream _oos, ObjectInputStream _ois, int _clientNumber, String _clientName)
   {
      this.cSocket = _cSocket;
      this.oos = _oos;
      this.ois = _ois;
      this.clientNumber = _clientNumber;
      this.clientName = _clientName;
   }
   
   // Accessors
   public Socket getClientSocket() {return this.cSocket;}
   public ObjectOutputStream getOos() {return this.oos;}
   public ObjectInputStream getOis() {return this.ois;}
   public int getClientNumber() {return this.clientNumber;}
   public String getClientName() {return this.clientName;}
   public String getCarFileName() {return this.carFileName;}
   public double getStartX() {return this.startX;}
   public double getStartY() {return this.startY;}
   public double getStartDegree() {return this.startDegree;}
   
   // Mutators
   public void setCarFileName(String _carFileName) {this.carFileName = _carFileName;}
   public void setStartX(double _startX) {this.startX = _startX;}
   public void setStartY(double _startY) {this.startY = _startY;}
   public void setStartDegree(double _startDegree) {this.startDegree = _startDegree;}
   
   /** 
   * Returns a formatted client identification information 
   *
   * @return client identification information
   */
   public String toString()
   {
      return String.format("Client%d, carFileName: %s", this.clientNumber, this.carFileName);
   }
}