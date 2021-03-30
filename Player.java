// JavaFX
import javafx.application.Application;
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
*  @version 16.03.2021
*/

public class Player extends Pane
{
   // Attributes
   
   // GameClient
   private GameClient gameClient = null;
   
   // Player
   private String playerName;
   private int playerNumber;
   
   // Car
      // Track border
   private double trackBorderX;
   private double trackBorderY;
      // Car Image
   private String carFileName;
   private Image carImage = null;
   private ImageView carImageView = null;
      // Car Dimensions
   private static double carHeight; // Pixels
   private static double carWidth; // Pixels
   private static double distanceBetweenWheels = 50; // Pixels
      // Car position
   private double carCenterX;
   private double carCenterY;
      // Car movement
   private Position carCenterLocation; // Top left corner (actual position of the image)
   private double carHeadingDegree;
   private Vector2 velocity; // Vector of velocity
   private Position wheelBase; // Center of the car (position of the center of the car)
   private double currentVelocity = 0.0;
   private double currentSteeringAngle = 0.0;
   private double carMaxSpeed = 135; // Pixels/Second
   private double carMaxReverseSpeed = -(carMaxSpeed / 2);
   private double steeringAngleMaximum = 30; // Degrees
   private double steeringStiffness = 30; // Higher = Stiffer
   private double deltaTime = 60;
      // Physics
   private double friction = 0.5; // Pixels/Frame
      // DEBUG
   private Circle centerCircle = null;
   
   /**
   * Constructor
   *
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
      this.gameClient = _gc;
      this.playerNumber = _playerNumber;
      this.playerName = "Player" + _playerNumber;
      this.trackBorderX = _trackWidth;
      this.trackBorderY = _trackHeight;
      this.carFileName = _carImageName;
      
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
      
      // Rotate car to face the right direction
      this.carHeadingDegree = _startRotation;
      carImageView.setRotate(carHeadingDegree);
      
      // Set car centering values
      carCenterX = carWidth / 2;
      carCenterY = carHeight / 2;
      
      // Set car start position
      double startX = _startX - carCenterX; // Centering cause translation is set for the origin (top left corner)
      double startY = _startY - carCenterY;
      
      carImageView.setTranslateX(startX);
      carImageView.setTranslateY(startY);
      //centerCircle = new Circle(40/2, 70/2, 5);
      //System.out.println("CarStartX: " + startX + " CarStartY: " + startY);
      
      // Record initial position of the car as a Position
      carCenterLocation = new Position(_startX, _startY);
      //System.out.println("Init car location " + carCenterLocation);
      
      // Add car image to THIS
      this.getChildren().addAll(carImageView /*, centerCircle*/);
      
      // Set wheel base
      wheelBase = new Position(_startX, _startY);
      //System.out.println("\nWheel base: " + wheelBase + "\nCar Height: " + carHeight);
   
      //System.out.println(this.playerName);
   }
   
   /** Returns the steering angle value*/
   public double getSteerAngle() {return this.currentSteeringAngle;}
   /** Returns the name of the car image file*/
   public String getCarFileName() {return this.carFileName;}
   
   public int getPlayerNumber() {return this.playerNumber;}
   
   public String getCoordinates()
   {
      return String.format("Player%d   iX: %f   iY:%f", playerNumber, carImageView.getX(), carImageView.getY());
   }
   
   public void setCoordinates(double _x, double _y, double _degree)
   {
      carImageView.setTranslateX(_x);
      carImageView.setTranslateY(_y);
      carImageView.setRotate(_degree);
   }
//    public void setStartingPosition(double _x, double _y, double _degree)
//    {
//       // Set car centering values
//       carCenterX = carWidth / 2;
//       carCenterY = carHeight / 2;      
//       
//       double x = _x - carCenterX;
//       double y = _y - carCenterY;
//       carImageView.setTranslateX(x);
//       carImageView.setTranslateY(y);
//       carImageView.setRotate(_degree);
//       carCenterLocation = new Position(_x, _y);
//       wheelBase = new Position(_x, _y);
//    }
   
   
   /**
   * Updates the position of the car
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
         if(currentVelocity > 0)
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
      //CHECK
      //System.out.println(currentVelocity);
      //System.out.println(currentSteeringAngle);
      
      // Calculate the position based on steering
      // Velocity is divided by the number of frames
      // Current velocity is in pixels/second
      calculateSteering(currentSteeringAngle, (currentVelocity / deltaTime));
   } // END update()
   
   /**
   * Calculates the position of the car on the next frame
   * Author of idea - http://engineeringdotnet.blogspot.com/2010/04/simple-2d-car-physics-in-games.html?m=1
   * Author of implementation - Artem Polkovnkov
   *
   * @param _trun shows in which direction the wheel is turned
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
      //CHECK
      //System.out.println(posVector);
      
      posVector.rotatedBy(carHeadingDegree); // ROTATE the position vector by the carHeadingDegree
      //CHECK
      //System.out.println(posVector);
      
         // Copy position of the car into a vector
      Vector2 centerOfTheCar = new Vector2(carCenterLocation.getX() , carCenterLocation.getY());
      //System.out.println("1: " + centerOfTheCar);
         // Add posVector to the position of the car
      centerOfTheCar.add(posVector);
         // Record the resulting position of the front wheels
      Position frontWheel = new Position(centerOfTheCar.getX() , centerOfTheCar.getY());
      //System.out.println(frontWheel);
      
      Vector2 centerOfTheCar2 = new Vector2(carCenterLocation.getX() , carCenterLocation.getY());
      //System.out.println("2: " + centerOfTheCar2);
      centerOfTheCar2.subtract(posVector);
      Position backWheel = new Position(centerOfTheCar2.getX() , centerOfTheCar2.getY());
      //System.out.println(backWheel);
      //CHECK
      //System.out.println("Back: " + backWheel + "\n" + "Front: " + frontWheel);
      
      //
      // WHEELS POSITION CALCULATED
      //
      
      
      
      
      
      
      // Calculate position of the wheels in the next frame
      //
      // Add vector of the movement to each wheels' position
      //
      
      ////BACK WHEEL////
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
         //System.out.println("NOT MOVING");
         backWheelVector.setX(backWheel.getX());
         backWheelVector.setY(backWheel.getY());
         
         dontChangeAngle = true;
      }
      //CHECK
      //System.out.println(backWheelVector);
      
      
      ////FRONT WHEEL////
         // Create frontWheelVector for calculations
      Vector2 frontWheelVector = new Vector2(frontWheel.getX() , frontWheel.getY());
         // Create vector for backWheelMovement
      Vector2 frontWheelMovement = new Vector2();
         // MULTIPLY frontWheelMovement by the currentVelocity
      frontWheelMovement.dot(_currentVelocity);
         // Rotate frontWheelMovement vector to match the car rotation (carHeadingDegree + (_turn * steerAngle))
      frontWheelMovement.rotatedBy(carHeadingDegree + _turn);
      //System.out.println(carHeadingDegree - (_turn * steerAngle));
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
      //CHECK
      //System.out.println(frontWheelVector);
      
      //
      // WHEELS POSITION IN THE NEXT FRAME CALCULATED
      //
      
      
      
      
      
      
      // Approximate the car position in the next frame based on the position of the wheels
      //
      //
      
      Vector2 tempFront = new Vector2(frontWheelVector);
      Vector2 tempBack = new Vector2(backWheelVector);
      
      ////carLocation = (frontWheel + backWheel) / 2;
      Vector2 carBody = tempFront.add(tempBack).dot(Math.pow(2, -1));
      //CHECK
      //System.out.println(carBody);
      
      // Add car movement to the car position
      // Record new car position into car and move the car
            
      // STOP AT BORDERS (WILL LIKELY BE CHANGED LATER)
      double xOffset = carWidth / 2; // -(carHeight / 4) to make it stop at the line
      double yOffset = carWidth;
      double minusXOffset = carWidth;
      double minusYOffset = carWidth;
      
      if(carBody.getX() >= trackBorderX + xOffset)
      {
         carBody.setX(trackBorderX + xOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getX() <= minusXOffset)
      {
         carBody.setX(minusXOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getY() >= trackBorderY + yOffset)
      {
         carBody.setY(trackBorderY + yOffset);
         currentVelocity *= 0.8;
      }
      if(carBody.getY() <= minusYOffset)
      {
         carBody.setY(minusYOffset);
         currentVelocity *= 0.8;
      }
      
      carCenterLocation.setX(carBody.getX());
      carCenterLocation.setY(carBody.getY());
      //CHECK
      //System.out.println(carCenterLocation + "\n" + carBody);
      
      carImageView.setTranslateX(carCenterLocation.getX() - (carWidth / 2));
      carImageView.setTranslateY(carCenterLocation.getY() - (carHeight / 2));
      //centerCircle.setTranslateX(carCenterLocation.getX() - carWidth);
      //centerCircle.setTranslateY(carCenterLocation.getY() - carHeight);
      
      //
      // CAR POSITION CALCULATED
      //
      
      
      
      
      
      
      // Find the new angle the car is facing
      //
      
      double fy = frontWheelVector.getY();
      double fx = frontWheelVector.getX();
      
      double by = backWheelVector.getY();
      double bx = backWheelVector.getX();
      
      double newCarHeading = Math.toDegrees(  Math.atan2(fy - by, fx - bx)  ) + 90;
      this.carHeadingDegree = newCarHeading;
      carImageView.setRotate(this.carHeadingDegree);
      //CHECK
      //String coordinates = String.format("Front wheel - X: %.2f Y: %.2f    Back wheel - X: %.2f Y: %.2f", frontWheelVector.getX(), frontWheelVector.getY(), backWheelVector.getX(), backWheelVector.getY());
      //System.out.println(coordinates);
      //System.out.println(newCarHeading);
      
      // SEND COORDINATES TO THE SERVER
      gameClient.sendCoordinatesToServer(carCenterLocation.getX() - (carWidth / 2), carCenterLocation.getY() - (carHeight / 2), this.carHeadingDegree);
   } // END calculateSteering()
   
   public String toString()
   {
      return String.format("Player: %s.   Car File Name: %s",this.playerName, this.carFileName);
   }
} // END Player