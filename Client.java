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
   
   public Client(Socket _cSocket, ObjectOutputStream _oos, ObjectInputStream _ois, int _clientNumber)
   {
      this.cSocket = _cSocket;
      this.oos = _oos;
      this.ois = _ois;
      this.clientNumber = clientNumber;
   }
   
   public Socket getClientSocket() {return this.cSocket;}
   public ObjectOutputStream getOos() {return this.oos;}
   public ObjectInputStream getOis() {return this.ois;}
   public int getClientNumber() {return this.clientNumber;}
}