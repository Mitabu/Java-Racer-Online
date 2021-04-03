/**
*  @author Artem Polkovnikov
*  @version 03.04.2021
*/

public class GameLogic
{
   /**
   * Checks if a point is on the line
   *
   * @param l1 line
   * @param p point
   */
   private static boolean onLine(CheckPoint l1, Position p)
   {
      if(p.getX() <= Math.max(l1.getX1(), l1.getX2()) && p.getX() <= Math.min(l1.getX1(), l1.getX2()) &&
        (p.getY() <= Math.max(l1.getY1(), l1.getY2()) && p.getY() <= Math.min(l1.getY1(), l1.getY2())))
      {
         return true;
      }
      return false;
   }
   
   /** 
   * Finds whethere two lines are collinear or not
   *
   * @param a point one
   * @param b point two
   * @param c point three
   */
   private static int direction(Position a, Position b, Position c)
   {
      double val = (b.getY()-a.getY()) * (c.getX()-b.getX()) - (b.getX()-a.getX()) * (c.getY()-b.getY());
      if (val == 0)
      {
         return 0;     //colinear
      }
      else if(val < 0)
      {
         return 2;    //anti-clockwise direction
      }
      return 1;    //clockwise direction
   }
   
   /**
   * Finds out whether two lines are intersecting
   * Adopted from https://www.tutorialspoint.com/Check-if-two-line-segments-intersect
   * 
   * @param l1 first line for intersection check
   * @param l2 second line for intersection check
   */
   public static boolean isIntersect(CheckPoint l1, CheckPoint l2)
   {
      Position l1_p1 = new Position(l1.getX1(), l1.getY1());
      Position l1_p2 = new Position(l1.getX2(), l1.getY2());
      
      Position l2_p1 = new Position(l2.getX1(), l2.getY1());
      Position l2_p2 = new Position(l2.getX2(), l2.getY2());
      
      //four direction for two lines and points of other line
      int dir1 = direction(l1_p1, l1_p2, l2_p1);
      int dir2 = direction(l1_p1, l1_p2, l2_p2);
      int dir3 = direction(l2_p1, l2_p2, l1_p1);
      int dir4 = direction(l2_p1, l2_p2, l1_p2);
      
      if(dir1 != dir2 && dir3 != dir4)
         return true; //they are intersecting
   
      if(dir1 == 0 && onLine(l1, l2_p1)) //when p2 of line2 are on the line1
         return true;
   
      if(dir2==0 && onLine(l1, l2_p2)) //when p1 of line2 are on the line1
         return true;
   
      if(dir3==0 && onLine(l2, l1_p1)) //when p2 of line1 are on the line2
         return true;
   
      if(dir4==0 && onLine(l2, l1_p2)) //when p1 of line1 are on the line2
         return true;
            
      return false;
   }
   
   /** 
   * Returns x length of a line
   *
   * @param x line
   */
   public static double getLineX(CheckPoint x)
   {
      return (Math.max(x.getX1(), x.getX2()) - Math.min(x.getX1(), x.getX2()));
   }
   
   /** 
   * Returns y length of a line
   *
   * @param y line
   */
   public static double getLineY(CheckPoint y)
   {
      return (Math.max(y.getY1(), y.getY2()) - Math.min(y.getY1(), y.getY2()));
   }
}