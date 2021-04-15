// Java
import java.io.*;

//XML DOM PARSER
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.xml.sax.*;

/**
*  @author Artem Polkovnikov
*  @version 14.04.2021
*/

/** Writes and reads xml settings files*/
public class XMLSettings
{
   // Attributes - properties
      // Socket
   
   /** Standard game server IP*/
   public String serverIP = "127.0.0.1";
   /** Server port used by both client and server*/
   public int serverPort = 42069;
      // Game Settings
   /** Standard player nickname*/
   public String nickname = " ";
   /** Standard car color*/
   public String carColor = "blue";
   
   // Private properties
   /** Full path to the xml settings file*/
   private String xmlFilePath = "";
   
   /**
   * Construcor that allows for writing standard settings in a selected location
   * 
   * @param _fileName full path to the xml settings file
   */
   public XMLSettings(String _fileName)
   {
      this.xmlFilePath = _fileName;
   }
   
   
   /**
   * Constructor with adjustable attributes for writing custom settings files
   * 
   * @param _fileName full path to the xml settings file
   * @param _serverPort server port number
   * @param _serverIP server IP address
   * @param _nickname player's nickname
   * @param _carColor color of the player's car
   */
   public XMLSettings(String _fileName, int _serverPort, String _serverIP, String _nickname, String _carColor)
   {
      this.xmlFilePath = _fileName;
      this.serverPort = _serverPort;
      this.serverIP = _serverIP;
      this.nickname = _nickname;
      this.carColor = _carColor;
   }
   
   /** Writes an xml settings file based on the settings saved in the XMLSettings class's attributes*/
   public void writeXML()
   {
      try {
         DocumentBuilderFactory dbFactory =
            DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.newDocument();
      
      // root element
         Element rootElement = doc.createElement("GameSettings");
         doc.appendChild(rootElement);
      
      // socket
         Element socket = doc.createElement("Socket");
         rootElement.appendChild(socket);
      
         // port
         Element port = doc.createElement("port");
         port.appendChild(doc.createTextNode(this.serverPort+""));
         socket.appendChild(port);
         // server ip
         Element serverIp = doc.createElement("serverIP");
         serverIp.appendChild(doc.createTextNode(this.serverIP+""));
         socket.appendChild(serverIp);
         
      // game settings
         Element gameSettings = doc.createElement("Game_Settings");
         rootElement.appendChild(gameSettings);
         
         // nickname
         Element nicknameE = doc.createElement("nickname");
         nicknameE.appendChild(doc.createTextNode(this.nickname+""));
         gameSettings.appendChild(nicknameE);
         
         // car color
         Element carColorE = doc.createElement("car_color");
         carColorE.appendChild(doc.createTextNode(this.carColor+""));
         gameSettings.appendChild(carColorE);
      
      // write the content into xml file
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         DOMSource source = new DOMSource(doc);
         StreamResult result = new StreamResult(new File(this.xmlFilePath));
         transformer.transform(source, result);
      
      // Output to console for testing
      //   StreamResult consoleResult = new StreamResult(System.out);
      //   transformer.transform(source, consoleResult);
      } catch (Exception e) {
         e.printStackTrace();
      }  
   }// end of writeXML
   
   /** 
   * Reads in an xml settings file and stores it's contents within the XMLSettings class.
   *
   * @return contents of the read xml settings file in a formatted String
   */
   public String readXML()
   {
      try{
         DocumentBuilderFactory dbfactory= DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = dbfactory.newDocumentBuilder();
         XPathFactory xpfactory = XPathFactory.newInstance();
         XPath path = xpfactory.newXPath();
      
         File f = new File(this.xmlFilePath);
         Document doc = builder.parse(f);
         
         // Port
         this.serverPort = Integer.parseInt(path.evaluate(
               "/GameSettings/Socket/port", doc));
         // IP    
         this.serverIP = path.evaluate(
               "/GameSettings/Socket/serverIP", doc);
         // Nickname
         this.nickname = path.evaluate(
               "/GameSettings/Game_Settings/nickname", doc);
         // Car color
         this.carColor = path.evaluate(
               "/GameSettings/Game_Settings/car_color", doc);
      
         return ("Current configuration: \nServer Port: " + this.serverPort + "    Server IP: " + this.serverIP + "\nNickname: " + this.nickname + "    Car color: " + this.carColor);         
      
      }
      catch(XPathExpressionException xpee)
      {
         System.out.println(xpee);
      }
      catch(ParserConfigurationException pce)
      {
         System.out.println(pce);
      }
      catch(SAXException saxe)
      {
         System.out.println(saxe);
      }
      catch(IOException ioe)
      {
         System.out.println(ioe);
      
      }
      
      return "ERROR";
   }//end of readXML
}