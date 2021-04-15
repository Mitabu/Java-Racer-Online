// JavaFX
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.stage.FileChooser.*;
import javafx.scene.image.*;
import javafx.scene.shape.*;
import javafx.animation.AnimationTimer;// Animation timer
import javafx.scene.input.KeyEvent;// Key Listener
// Java
import java.io.*;
import java.util.*;
import java.util.ArrayList;

/**
*  @author Artem Polkovnikov
*  @version 14.04.2021
*/

/** A pane that contains the player's car and calculates it's movement based on player inputs*/
public class Player extends Pane
{
   // Attributes
   
   // GameClient
   /** Game client object for sending coordinates to the game server*/
   private GameClient gameClient = null;
   
   // Player
   /** Player's nickname*/
   private String playerName;
   /** Player's ID number*/
   private int playerNumber;
   
   // Car
   
      // Track border
   /** X coordinate of the track border*/
   private double trackBorderX;
   /** Y coordinate of the track border*/
   private double trackBorderY;
   
      // Car Image
   /** Player's car file name*/
   private String carFileName;
   /** Player's car image*/
   private Image carImage = null;
   /** Player's car imageView*/
   private ImageView carImageView = null;
   
      // Car Dimensions
   /** Height of the player's car in pixels*/
   private static double carHeight; // Pixels
   /** Width of the player's car in pixels*/
   private static double carWidth; // Pixels
   /** Distance between the wheels of the player's car in pixels*/
   private static double distanceBetweenWheels = 50; // Pixels
      
      // Car position
   /** Car center X coordinate*/
   private double carCenterX;
   /** Car center Y coordinate*/
   private double carCenterY;
      
      // Car movement
   /** Car center position*/
   private Position carCenterLocation; // Top left corner (actual position of the image)
   /** Car's rotation in degrees*/
   private double carHeadingDegree;
   /** Vector of the car's velocity*/
   private Vector2 velocity; // Vector of velocity
   /** Car's velocity in a given frame*/
   private double currentVelocity = 0.0;
   /** Car's steering angle in a given frame*/
   private double currentSteeringAngle = 0.0;
   /** Car's maximum speed in pixels per second*/
   private double carMaxSpeed = 75; // Pixels/Second
   /** Car's maximum reverse speed (half of the maximum speed)*/
   private double carMaxReverseSpeed = -(carMaxSpeed / 2);
   /** Maximum steering angle*/
   private double steeringAngleMaximum = 30; // Degrees
   /** Value of the steering stiffness (higher value = takes longer to engage a full turn)*/
   private double steeringStiffness = 15; // Higher = Stiffer
   /** Value by which velocity is divided in order to get velocity in a given frame*/
   private double deltaTime = 10;
      
      // Physics
   /** Friction value that stops the car when car isn't accelerating*/
   private double friction = 0.5; // Pixels/Frame
   
   
   /**
   * Constructor
   *
   * @param _gc game client for transmitting coordinates to the server
   * @param _ownerStage the stage on which the player is show (a reference for alerts)
   * @param _playerNumber the number of the player
   * @param _carImageName the name of the car image file
   * @param _startX initial position of the car on the X-axis in pixels
   * @param _startY initial position of the car on the Y-axis in pixels
   * @param _startRotation initial rotation of the car image in degrees
   * @param _trackWidth the width of the ridable area
   * @param _trackHeight the height of the ridable area
   */
   public Player(GameClient _gc, Stage _ownerStage, int _playerNumber, String _carImageName, double _startX, double _startY, double _startRotation, double _trackWidth, double _trackHeight)
   {
      // Initiate player parameters
      this.gameClient = _gc;
      this.playerNumber = _playerNumber;
      this.playerName = "Player" + _playerNumber;
      this.trackBorderX = _trackWidth;
      this.trackBorderY = _trackHeight;
      this.carFileName = _carImageName;
      
      // Fetch car image and get its dimentions
      try
      {
         FileInputStream fis = new FileInputStream(new File(_carImageName));
         carImage = new Image(fis);
         carImageView = new ImageView(carImage);
         
         carWidth = carImage.getWidth();
         carHeight = carImage.getHeight();
         
         fis.close();
      }
      catch(FileNotFoundException fnfe)
      {
         DisplayMessage.showAlert(_ownerStage, AlertType.ERROR, "An error occured when fetching car image for " + this.playerName, fnfe+"");
      }
      catch(IOException ioe)
      {
         DisplayMessage.showAlert(_ownerStage, AlertType.ERROR, "An error occured when fetching car image for " + this.playerName, ioe+"");
      }
      
      // Rotate car to face the starting direction
      this.carHeadingDegree = _startRotation;
      carImageView.setRotate(carHeadingDegree);
      
      // Set car centering values
      carCenterX = carWidth / 2;
      carCenterY = carHeight / 2;
      
      // Set car start position
      double startX = _startX - carCenterX; // Centering cause translation is set for the origin (top left corner)
      double startY = _startY - carCenterY;
      
      // Translate the car's imageView to the starting position
      carImageView.setTranslateX(startX);
      carImageView.setTranslateY(startY);
      
      // Record initial position of the car as a Position
      carCenterLocation = new Position(_startX, _startY);
      
      // Add car image to this pane
      this.getChildren().addAll(carImageView /*, centerCircle*/);
   }
   
   /** 
   * Returns the steering angle value
   *
   * @return current steering angle value
   */
   public double getSteerAngle() {return this.currentSteeringAngle;}
   
   /**
   * Returns the name of the car image file
   *
   * @return car file name
   */
   public String getCarFileName() {return this.carFileName;}
   
   /**
   * Returns player's ID number
   *
   * @return player's ID number
   */
   public int getPlayerNumber() {return this.playerNumber;}
   
   /**
   * Returns current position alongside player's ID
   *
   * @return player's ID and coordinates
   */
   public String getCoordinates()
   {
      return String.format("Player%d   iX: %f   iY:%f", playerNumber, carImageView.getX(), carImageView.getY());
   }
   
   /** Sets player's car imageView invisible*/
   public void hideCar()
   {
      Platform.runLater
      (
         new Runnable()
         {
            public void run()
            {
               carImageView.setVisible(false);
            }
         }
      );
   }
   
   /**
   * Directly sets coordinates of the player's car
   *
   * @param _x X coordinate
   * @param _y Y coordinate
   * @param _degree rotation in degrees
   */
   public void setCoordinates(double _x, double _y, double _degree)
   {
      Platform.runLater
      (
         new Runnable()
         {
            public void run()
            {
               carImageView.setTranslateX(_x);
               carImageView.setTranslateY(_y);
               carImageView.setRotate(_degree);
            }
         }
      );
   }   
   
   /**
   * Updates the position of the car using car movement algorithm
   * 
   * @param _turn shows in which direction the wheel is turned
   * @param _velocity the direction in which the velocity is added (if gas is pressed +1, if brake is pressed -1)
   */
   public void update(int _turn, double _velocity)
   {         
      // Set up the car's current velocity
      
      if(_velocity > 0)
      {
         currentVelocity += _velocity;
         
         if(currentVelocity >= carMaxSpeed)// Speed limit
         {
            currentVelocity = carMaxSpeed;
         }
      }
      else if(_velocity < 0)
      {
         if(currentVelocity > 0)// Breaking
         {
            currentVelocity += _velocity;
         }
         else if(currentVelocity <= 0)// Reversing
         {
            currentVelocity += (_velocity / 5);
         }
         
         if(currentVelocity <= carMaxReverseSpeed)// Reversing limit
         {
            currentVelocity = carMaxReverseSpeed;
         }
      }
      else if(_velocity == 0)// Friction
      {
         // Direction of deceleration
         if(currentVelocity > -0.5 && currentVelocity < 0.5)
         {
            currentVelocity = 0;
         }
         else if(currentVelocity > 0)
         {
            currentVelocity -= friction;
         }
         else if(currentVelocity < 0)
         {
            currentVelocity += friction;
         }
      }
      // END of veclocity set up
      
      // Set up the car's steering angle
      if(_turn > 0) // Turn right
      {
         currentSteeringAngle += _turn*(steeringAngleMaximum / steeringStiffness);
         
         if(currentSteeringAngle >= steeringAngleMaximum)
         {
            currentSteeringAngle = steeringAngleMaximum;
         }
      }
      else if(_turn < 0) // Turn left
      {
         currentSteeringAngle += _turn*(steeringAngleMaximum / steeringStiffness);
         
         if(currentSteeringAngle <= -steeringAngleMaximum)
         {
            currentSteeringAngle = -steeringAngleMaximum;
         }
      }
      else if(_turn == 0) // Idle return to the normal steering
      {
         if(currentSteeringAngle > 0) // Positive steering angle
         {
            currentSteeringAngle -= steeringAngleMaximum / steeringStiffness;
         
            if(currentSteeringAngle <= 0)
            {
               currentSteeringAngle = 0;
            }
         }
         else if(currentSteeringAngle < 0) // Negative steering angle
         {
            currentSteeringAngle += steeringAngleMaximum / steeringStiffness;
         
            if(currentSteeringAngle >= 0)
            {
               currentSteeringAngle = 0;
            }
         }
      }
      
      // Calculate the position based on steering
      // Velocity is divided by the delta of time
      // Current velocity is in pixels/second
      calculateSteering(currentSteeringAngle, (currentVelocity / deltaTime));
   } // END update()
   
   /**
   * Calculates the position of the car on the next frame
   * Author of idea - http://engineeringdotnet.blogspot.com/2010/04/simple-2d-car-physics-in-games.html?m=1
   * Author of implementation - Artem Polkovnkov
   *
   * @param _turn shows in which direction the wheel is turned
   * @param _currentVelocity the velocity of the car in the frame
   */
   private void calculateSteering(double _turn, double _currentVelocity)
   {
      // Calculate the position of the wheels in the current frame
      //
      // posVector - unit vector MULTIPLIED by the (distanceBetweenWheels / 2) AND ROTATED by the carHeadingDegree
      //
      // wheel = carCenter + posVector;
      //
      
      Vector2 posVector = new Vector2(); // Create horizontal unit vector
      posVector.dot(distanceBetweenWheels / 2); // MULTIPLY unit vector by the distance between wheels / 2
      posVector.rotatedBy(carHeadingDegree); // ROTATE the position vector by the carHeadingDegree
      
      // Front wheel position
         // Copy position of the car into a vector
      Vector2 centerOfTheCar = new Vector2(carCenterLocation.getX() , carCenterLocation.getY());
         // Add posVector to the position of the car
      centerOfTheCar.add(posVector);
         // Record the resulting position of the front wheels
      Position frontWheel = new Position(centerOfTheCar.getX() , centerOfTheCar.getY());
      
      // Back wheel position
      Vector2 centerOfTheCar2 = new Vector2(carCenterLocation.getX() , carCenterLocation.getY());
      centerOfTheCar2.subtract(posVector);
      Position backWheel = new Position(centerOfTheCar2.getX() , centerOfTheCar2.getY());
      //
      // WHEELS POSITION CALCULATED
      //
      
      
      
      // Calculate position of the wheels in the next frame
      //
      // Add vector of the movement to each wheels' position
      //
      
      // Back wheel
         // Create backWheelVector for calculations
      Vector2 backWheelVector = new Vector2(backWheel.getX() , backWheel.getY());
         // Create vector for backWheelMovement
      Vector2 backWheelMovement = new Vector2();
         // MULTIPLY backWheelMovement by the currentVelocity
      backWheelMovement.dot(_currentVelocity);
         // Rotate backWheelMovement vector to match the car rotation (carHeadingDegree)
      backWheelMovement.rotatedBy(carHeadingDegree);
         // Add movement to the backWheelVector to get the new position
      backWheelVector.add(backWheelMovement);
      
      // Reset backwheel to where it was if it wasn't moved
      boolean dontChangeAngle = false;
      
      if(Double.isNaN(backWheelVector.getX()) || Double.isNaN(backWheelVector.getY()))
      {
         backWheelVector.setX(backWheel.getX());
         backWheelVector.setY(backWheel.getY());
         
         dontChangeAngle = true;
      }
      
      // Front wheel
         // Create frontWheelVector for calculations
      Vector2 frontWheelVector = new Vector2(frontWheel.getX() , frontWheel.getY());
         // Create vector for backWheelMovement
      Vector2 frontWheelMovement = new Vector2();
         // MULTIPLY frontWheelMovement by the currentVelocity
      frontWheelMovement.dot(_currentVelocity);
         // Rotate frontWheelMovement vector to match the car rotation (carHeadingDegree + (_turn * steerAngle))
      frontWheelMovement.rotatedBy(carHeadingDegree + _turn);
         // Add movement to the backWheelVector
      frontWheelVector.add(frontWheelMovement);
      
      // Reset frontWheel to where it was if it wasn't moved
      if(Double.isNaN(frontWheelVector.getX()) || Double.isNaN(frontWheelVector.getY()))
      {
         //System.out.println("NOT MOVING");
         frontWheelVector.setX(frontWheel.getX());
         frontWheelVector.setY(frontWheel.getY());
         dontChangeAngle = true;
      }      
      //
      // WHEELS POSITION IN THE NEXT FRAME CALCULATED
      //
      
      
      
      // Approximate the car position in the next frame based on the position of the wheels
      //
      //
      Vector2 tempFront = new Vector2(frontWheelVector);
      Vector2 tempBack = new Vector2(backWheelVector);
      
      Vector2 carBody = tempFront.add(tempBack).dot(Math.pow(2, -1));
            
      // Stop at track borders
      double xOffset = carWidth; // -(carHeight / 4) to make it stop at the line
      double yOffset = 0;
      double minusXOffset = carWidth;
      double minusYOffset = carWidth / 2;
      
      if(carBody.getX() >= trackBorderX - xOffset)
      {
         carBody.setX(trackBorderX - xOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getX() <= minusXOffset)
      {
         carBody.setX(minusXOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getY() >= trackBorderY - yOffset)
      {
         carBody.setY(trackBorderY - yOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getY() <= -minusYOffset)
      {
         carBody.setY(-minusYOffset);
         currentVelocity *= 0.8;
      }
      
      // Add car movement to the car position
      // Record new car position into car and move the car
      carCenterLocation.setX(carBody.getX());
      carCenterLocation.setY(carBody.getY());
      //
      // CAR POSITION CALCULATED
      //
      
      
      
      // Find the new angle the car is facing
      //
      double fy = frontWheelVector.getY();
      double fx = frontWheelVector.getX();
      
      double by = backWheelVector.getY();
      double bx = backWheelVector.getX();
      
      // Get new car heading
      double newCarHeading = Math.toDegrees(  Math.atan2(fy - by, fx - bx)  ) + 90;
      this.carHeadingDegree = newCarHeading;
      
      // Set new car coordinates and heading
      Platform.runLater
      (
         new Runnable()
         {
            public void run()
            {
               carImageView.setTranslateX(carCenterLocation.getX() - (carWidth / 2));
               carImageView.setTranslateY(carCenterLocation.getY() - (carHeight / 2));
               carImageView.setRotate(carHeadingDegree);
            }
         }
      );
      
      // Send calculated coordinates to the server
      gameClient.sendCoordinatesToServer(carCenterLocation.getX() - (carWidth / 2), carCenterLocation.getY() - (carHeight / 2), this.carHeadingDegree);
   } // END calculateSteering()
   
   /**
   * Returns formatted player information
   *
   * @return formatted player information
   */
   public String toString()
   {
      return String.format("Player: %s.   Car File Name: %s",this.playerName, this.carFileName);
   }
} // END Player