// Java
import java.io.*;
import java.util.*;
import java.net.*;

public class Client
{
   // Attributes
   private Socket cSocket = null;
   private ObjectOutputStream oos = null;
   private ObjectInputStream ois = null;
   private int clientNumber;
   private String carFileName;
   private double startX;
   private double startY;
   private double startDegree;
   
   public Client()
   {
      
   }
   public Client(Socket _cSocket, ObjectOutputStream _oos, ObjectInputStream _ois, int _clientNumber)
   {
      this.cSocket = _cSocket;
      this.oos = _oos;
      this.ois = _ois;
      this.clientNumber = _clientNumber;
   }
   
   public Socket getClientSocket() {return this.cSocket;}
   public ObjectOutputStream getOos() {return this.oos;}
   public ObjectInputStream getOis() {return this.ois;}
   public int getClientNumber() {return this.clientNumber;}
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
      return String.format("Client%d, carFileName: %s", this.clientNumber, this.carFileName);
   }
}